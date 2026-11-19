package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.core.EmailOtpCoordinator
import com.example.core.SuperAdminCredentialStore
import com.example.core.configureAppCheck
import com.example.data.AiToolsRepository
import com.example.data.MvciRepository
import com.example.data.WebsiteConnectorProbe
import com.example.ui.auth.SuperAdminLoginScreen
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.auth.onGoogleSignInClicked
import com.example.ui.screens.MvciAppShell
import com.example.ui.theme.MvciControlTheme
import com.example.viewmodel.MvciViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure Firebase App Check with launch intent before setContent, ViewModels, or Firebase calls.
        configureAppCheck(this, intent)

        setContent {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { Firebase.auth }
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val credentialStore = remember(context) { SuperAdminCredentialStore(context) }
    val otpCoordinator = remember(credentialStore) { EmailOtpCoordinator(credentialStore) }

    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var sessionUnlocked by remember { mutableStateOf(false) }
    var pendingLoginAudit by remember { mutableStateOf(false) }
    var isConnectingCloud by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    // Attempt silent session restoration automatically without showing a separate Google Workspace screen.
    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { currentUser = auth.currentUser },
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    val user = currentUser
    if (!sessionUnlocked || user == null) {
        MvciControlTheme {
            SuperAdminLoginScreen(
                instituteName = "Maa Vaishno Computer Institute",
                directorName = "Rishu Singh",
                registeredEmail = credentialStore.recoveryEmail(),
                isConnecting = isConnectingCloud,
                externalError = loginError,
                onSubmitCredentials = { inputUser, inputPassword ->
                    val ok = credentialStore.verifyLogin(inputUser, inputPassword)
                    if (ok) {
                        loginError = null
                        if (auth.currentUser != null) {
                            currentUser = auth.currentUser
                            pendingLoginAudit = true
                            sessionUnlocked = true
                        } else {
                            isConnectingCloud = true
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isConnectingCloud = false
                                    currentUser = auth.currentUser
                                    pendingLoginAudit = true
                                    sessionUnlocked = true
                                },
                                onAuthError = { msg ->
                                    isConnectingCloud = false
                                    loginError = msg
                                },
                                scope = scope,
                                onAuthCancelled = {
                                    isConnectingCloud = false
                                    loginError = "Cloud workspace session needed to sync. Tap Login again to authorize."
                                }
                            )
                        }
                    }
                    ok
                },
                onRequestOtp = { email ->
                    otpCoordinator.requestOtpForRegisteredEmail(email)
                },
                onVerifyOtpReset = { otpCode, newPassword ->
                    otpCoordinator.verifyOtpAndResetPassword(otpCode, newPassword)
                        .map { "Password reset verified via Email OTP! Log in with your new password." }
                }
            )
        }
    } else {
        val uid = user.uid
        key(uid) {
            val mvciViewModel: MvciViewModel = viewModel(
                key = uid,
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY]) {
                            "APPLICATION_KEY missing from CreationExtras"
                        }
                        val databaseId = app.getString(R.string.firestore_database_id)
                        val db = FirebaseFirestore.getInstance(databaseId)
                        val repo = MvciRepository(db = db, auth = Firebase.auth)
                        MvciViewModel(
                            repository = repo,
                            probe = WebsiteConnectorProbe(),
                            aiRepository = AiToolsRepository(),
                            currentUserId = uid,
                            credentialStore = credentialStore,
                            otpCoordinator = otpCoordinator
                        )
                    }
                }
            )
            LaunchedEffect(mvciViewModel, pendingLoginAudit) {
                if (pendingLoginAudit) {
                    mvciViewModel.recordLoginUnlocked(credentialStore.activeUsername())
                    pendingLoginAudit = false
                }
            }
            MvciAppShell(
                viewModel = mvciViewModel,
                onLockConsole = {
                    mvciViewModel.lockSuperAdmin()
                    sessionUnlocked = false
                }
            )
        }
    }
}

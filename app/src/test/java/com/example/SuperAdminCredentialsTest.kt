package com.example

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import com.example.core.EmailOtpCoordinator
import com.example.core.OtpEmailTransport
import com.example.core.SuperAdminCredentialManager
import com.example.core.SuperAdminCredentialStore
import com.example.model.WorkspaceConfig
import com.example.ui.auth.SuperAdminLoginScreen
import com.example.ui.screens.SuperAdminCredentialsSettingsCard
import com.example.ui.theme.MvciControlTheme
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SuperAdminCredentialsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun credentialManager_verifiesDefaultUpdatesAndResets() {
        val defaultConfig = WorkspaceConfig()
        assertTrue(SuperAdminCredentialManager.verifyCredentials("Rishu@2050", "Rishu9044", defaultConfig))
        assertFalse(SuperAdminCredentialManager.verifyCredentials("Rishu@2050", "WrongPass", defaultConfig))
        assertFalse(SuperAdminCredentialManager.verifyCredentials("OtherUser", "Rishu9044", defaultConfig))

        val updatedMap = SuperAdminCredentialManager.createCredentialUpdateMap("RishuAdminNew", "Secret2027")
        val updatedConfig = defaultConfig.copy(
            adminUsername = updatedMap["adminUsername"] as String,
            adminPasswordSalt = updatedMap["adminPasswordSalt"] as String,
            adminPasswordHash = updatedMap["adminPasswordHash"] as String
        )
        assertNotEquals(SuperAdminCredentialManager.DEFAULT_ADMIN_SALT, updatedConfig.adminPasswordSalt)
        assertTrue(SuperAdminCredentialManager.verifyCredentials("RishuAdminNew", "Secret2027", updatedConfig))
        assertFalse(SuperAdminCredentialManager.verifyCredentials("Rishu@2050", "Rishu9044", updatedConfig))

        val resetMap = SuperAdminCredentialManager.defaultCredentialUpdateMap()
        assertEquals("Rishu@2050", resetMap["adminUsername"])
        assertEquals("rsingh13151@gmail.com", resetMap["recoveryEmail"])
        val resetConfig = updatedConfig.copy(
            adminUsername = resetMap["adminUsername"] as String,
            adminPasswordSalt = resetMap["adminPasswordSalt"] as String,
            adminPasswordHash = resetMap["adminPasswordHash"] as String
        )
        assertTrue(SuperAdminCredentialManager.verifyCredentials("Rishu@2050", "Rishu9044", resetConfig))
    }

    @Test
    fun emailOtpCoordinator_sendsToRegisteredEmailAndResetsPassword() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val store = SuperAdminCredentialStore(app)
        store.clearPendingOtp()
        var capturedOtp: String? = null
        var capturedRecipient: String? = null

        val fakeTransport = object : OtpEmailTransport {
            override suspend fun sendOtpEmail(
                recipientEmail: String,
                otpCode: String,
                instituteName: String,
                webhookOverride: String
            ): Result<String> {
                capturedRecipient = recipientEmail
                capturedOtp = otpCode
                return Result.success("OTP sent to $recipientEmail")
            }
        }
        val coordinator = EmailOtpCoordinator(store, fakeTransport)

        val wrongEmailRes = coordinator.requestOtpForRegisteredEmail("intruder@example.com")
        assertTrue(wrongEmailRes.isFailure)

        val sendRes = coordinator.requestOtpForRegisteredEmail("rsingh13151@gmail.com")
        assertTrue(sendRes.isSuccess)
        assertEquals("rsingh13151@gmail.com", capturedRecipient)
        assertNotNull(capturedOtp)
        assertEquals(6, capturedOtp!!.length)

        val wrongOtpRes = coordinator.verifyOtpAndResetPassword("000000", "NewPass9090")
        assertTrue(wrongOtpRes.isFailure)

        val validResetRes = coordinator.verifyOtpAndResetPassword(capturedOtp!!, "RishuReset@2026")
        assertTrue(validResetRes.isSuccess)
        assertTrue(store.verifyLogin("Rishu@2050", "RishuReset@2026"))
        assertFalse(store.verifyLogin("Rishu@2050", "Rishu9044"))
        assertTrue(store.hasPendingCloudSync())
    }

    @Test
    fun superAdminLoginScreen_hasNoGoogleWorkspaceOptionAndSupportsOtpReset() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val store = SuperAdminCredentialStore(app)
        store.saveCredentialsLocal(
            username = SuperAdminCredentialManager.DEFAULT_ADMIN_USERNAME,
            salt = SuperAdminCredentialManager.DEFAULT_ADMIN_SALT,
            hash = SuperAdminCredentialManager.DEFAULT_ADMIN_PASSWORD_HASH,
            recoveryEmail = "rsingh13151@gmail.com",
            pendingCloudSync = false
        )
        store.clearPendingOtp()

        var capturedOtp: String? = null
        val fakeTransport = object : OtpEmailTransport {
            override suspend fun sendOtpEmail(
                recipientEmail: String,
                otpCode: String,
                instituteName: String,
                webhookOverride: String
            ): Result<String> {
                capturedOtp = otpCode
                return Result.success("6-digit OTP sent to $recipientEmail.")
            }
        }
        val coordinator = EmailOtpCoordinator(store, fakeTransport)
        var unlocked = false

        composeRule.setContent {
            MvciControlTheme {
                SuperAdminLoginScreen(
                    instituteName = "Maa Vaishno Computer Institute",
                    directorName = "Rishu Singh",
                    registeredEmail = "rsingh13151@gmail.com",
                    onSubmitCredentials = { u, p ->
                        val ok = store.verifyLogin(u, p)
                        unlocked = ok
                        ok
                    },
                    onRequestOtp = { email -> coordinator.requestOtpForRegisteredEmail(email) },
                    onVerifyOtpReset = { otp, newPass ->
                        coordinator.verifyOtpAndResetPassword(otp, newPass)
                            .map { "Password reset verified via Email OTP!" }
                    }
                )
            }
        }

        // Verify no Google Workspace / Sign in with Google button exists
        composeRule.onNodeWithText("Google Workspace", substring = true, ignoreCase = true).assertDoesNotExist()
        composeRule.onNodeWithText("Sign in with Google", substring = true, ignoreCase = true).assertDoesNotExist()

        // Open Forgot Password Email OTP dialog and reset password
        composeRule.onNodeWithTag("forgot_password_otp_button").performScrollTo().performClick()
        composeRule.onNodeWithTag("send_otp_email_button").performClick()
        composeRule.waitUntil(3000) { capturedOtp != null }

        composeRule.onNodeWithTag("reset_otp_code_input").performTextInput(capturedOtp!!)
        composeRule.onNodeWithTag("otp_new_password_input").performTextInput("Rishu@New99")
        composeRule.onNodeWithTag("otp_confirm_password_input").performTextInput("Rishu@New99")
        composeRule.onNodeWithTag("verify_otp_reset_button").performClick()

        composeRule.waitUntil(3000) { store.verifyLogin("Rishu@2050", "Rishu@New99") }

        // Now log in with the newly reset password
        composeRule.onNodeWithTag("super_admin_username_input").performScrollTo().performTextInput("Rishu@2050")
        composeRule.onNodeWithTag("super_admin_password_input").performScrollTo().performTextInput("Wrong123")
        composeRule.onNodeWithTag("super_admin_login_button").performScrollTo().performClick()
        assertFalse(unlocked)

        composeRule.onNodeWithTag("super_admin_password_input").performScrollTo().performTextReplacement("Rishu@New99")
        composeRule.onNodeWithTag("super_admin_login_button").performScrollTo().performClick()
        assertTrue(unlocked)
    }

    @Test
    fun superAdminSettingsCard_updatesAndResetsInSettings() {
        var updatedCreds: Triple<String, String, String>? = null
        var resetTriggered = false
        composeRule.setContent {
            MvciControlTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    SuperAdminCredentialsSettingsCard(
                        config = WorkspaceConfig(),
                        onUpdateCredentials = { cur, newU, newP ->
                            updatedCreds = Triple(cur, newU, newP)
                        },
                        onResetCredentials = {
                            resetTriggered = true
                        }
                    )
                }
            }
        }

        composeRule.onNodeWithTag("settings_current_password_input").performScrollTo().performTextInput("Rishu9044")
        composeRule.onNodeWithTag("settings_new_username_input").performScrollTo().performTextReplacement("Rishu@2099")
        composeRule.onNodeWithTag("settings_new_password_input").performScrollTo().performTextInput("NewPass2099")
        composeRule.onNodeWithTag("settings_confirm_password_input").performScrollTo().performTextInput("NewPass2099")
        composeRule.onNodeWithTag("update_super_admin_creds_btn").performScrollTo().performClick()

        assertEquals(Triple("Rishu9044", "Rishu@2099", "NewPass2099"), updatedCreds)

        composeRule.onNodeWithTag("reset_super_admin_creds_btn").performScrollTo().performClick()
        composeRule.onNodeWithText("Reset Credentials").performClick()
        assertTrue(resetTriggered)
    }
}

package com.example.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MvciCyanAccent
import com.example.ui.theme.MvciEmerald
import com.example.ui.theme.MvciRoseError
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordOtpDialog(
    registeredEmail: String,
    onRequestOtp: suspend (String) -> Result<String>,
    onVerifyOtpReset: suspend (otpCode: String, newPassword: String) -> Result<String>,
    onResetCompleted: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(1) }
    var emailInput by remember(registeredEmail) { mutableStateOf(registeredEmail) }
    var otpCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPasswords by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSending && !isVerifying) onDismiss() },
        title = {
            Text(if (step == 1) "Forgot Password — Email OTP" else "Verify OTP & Reset Password")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (step == 1) {
                    Text(
                        "Enter your registered recovery email ($registeredEmail) to receive a 6-digit password reset OTP.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            errorMessage = null
                        },
                        label = { Text("Registered Email ID") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_recovery_email_input")
                    )
                } else {
                    Surface(
                        color = MvciEmerald.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp)) {
                            Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = MvciEmerald, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = statusMessage ?: "6-digit OTP sent to $registeredEmail (valid for 10 min).",
                                style = MaterialTheme.typography.labelMedium,
                                color = MvciCyanAccent
                            )
                        }
                    }
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { input ->
                            otpCode = input.filter { it.isDigit() }.take(6)
                            errorMessage = null
                        },
                        label = { Text("6-Digit Email OTP") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_otp_code_input")
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            errorMessage = null
                        },
                        label = { Text("New Password (min 6 chars)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPasswords = !showPasswords }) {
                                Icon(
                                    if (showPasswords) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPasswords) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (showPasswords) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_new_password_input")
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = { Text("Confirm New Password") },
                        visualTransformation = if (showPasswords) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_confirm_password_input")
                    )
                    OutlinedButton(
                        onClick = {
                            if (!isSending) {
                                errorMessage = null
                                isSending = true
                                scope.launch {
                                    onRequestOtp(emailInput.trim())
                                        .onSuccess { msg -> statusMessage = msg }
                                        .onFailure { err -> errorMessage = err.localizedMessage ?: "Failed to resend OTP." }
                                    isSending = false
                                }
                            }
                        },
                        enabled = !isSending && !isVerifying,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isSending) "Resending OTP..." else "Resend OTP to $registeredEmail")
                    }
                }

                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = MvciRoseError,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (step == 1) {
                Button(
                    onClick = {
                        errorMessage = null
                        isSending = true
                        scope.launch {
                            onRequestOtp(emailInput.trim())
                                .onSuccess { msg ->
                                    statusMessage = msg
                                    step = 2
                                }
                                .onFailure { err ->
                                    errorMessage = err.localizedMessage ?: "Unable to send OTP email."
                                }
                            isSending = false
                        }
                    },
                    enabled = !isSending && emailInput.isNotBlank(),
                    modifier = Modifier.testTag("send_otp_email_button")
                ) {
                    if (isSending) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(6.dp))
                        Text("Sending OTP...")
                    } else {
                        Text("Send OTP")
                    }
                }
            } else {
                Button(
                    onClick = {
                        when {
                            otpCode.length != 6 -> errorMessage = "Enter the 6-digit OTP sent to $registeredEmail."
                            newPassword.length < 6 -> errorMessage = "New password must be at least 6 characters."
                            newPassword != confirmPassword -> errorMessage = "New password and confirmation do not match."
                            else -> {
                                errorMessage = null
                                isVerifying = true
                                scope.launch {
                                    onVerifyOtpReset(otpCode, newPassword)
                                        .onSuccess { msg ->
                                            isVerifying = false
                                            onResetCompleted(msg)
                                        }
                                        .onFailure { err ->
                                            isVerifying = false
                                            errorMessage = err.localizedMessage ?: "OTP verification failed."
                                        }
                                }
                            }
                        }
                    },
                    enabled = !isVerifying,
                    modifier = Modifier.testTag("verify_otp_reset_button")
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(6.dp))
                        Text("Resetting...")
                    } else {
                        Text("Verify & Reset")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { if (!isSending && !isVerifying) onDismiss() },
                enabled = !isSending && !isVerifying
            ) {
                Text("Cancel")
            }
        }
    )
}

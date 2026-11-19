package com.example.core

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

interface OtpEmailTransport {
    suspend fun sendOtpEmail(
        recipientEmail: String,
        otpCode: String,
        instituteName: String,
        webhookOverride: String
    ): Result<String>
}

class OkHttpOtpEmailTransport(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) : OtpEmailTransport {

    override suspend fun sendOtpEmail(
        recipientEmail: String,
        otpCode: String,
        instituteName: String,
        webhookOverride: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val customWebhook = webhookOverride.trim().ifBlank {
            BuildConfig.OTP_WEBHOOK_URL.trim().takeUnless { it.equals("UNCONFIGURED", ignoreCase = true) }.orEmpty()
        }
        val resendKey = BuildConfig.RESEND_API_KEY.trim()
            .takeUnless { it.isBlank() || it == "MY_RESEND_API_KEY" }
            .orEmpty()

        when {
            customWebhook.startsWith("https://", ignoreCase = true) -> {
                val payload = JSONObject().apply {
                    put("email", recipientEmail)
                    put("otp", otpCode)
                    put("institute", instituteName)
                    put("purpose", "SUPER_ADMIN_PASSWORD_RESET")
                    put("expiresMinutes", 10)
                }
                val request = Request.Builder()
                    .url(customWebhook)
                    .post(payload.toString().toRequestBody(JSON_MEDIA))
                    .build()
                executeHttp(request, "OTP sent to $recipientEmail via MVCI server webhook.")
            }
            resendKey.isNotEmpty() -> {
                val htmlBody = """
                    <div style="font-family:Arial,sans-serif;max-width:520px;margin:0 auto;padding:20px;border:1px solid #e2e8f0;border-radius:12px;">
                      <h2 style="color:#0f172a;margin:0 0 8px;">$instituteName — Super Admin OTP</h2>
                      <p style="color:#334155;font-size:14px;">Use this one-time verification code to reset your MVCI Super Admin password:</p>
                      <div style="background:#0f172a;color:#fbbf24;font-size:28px;font-weight:700;letter-spacing:6px;text-align:center;padding:16px;border-radius:10px;margin:16px 0;">$otpCode</div>
                      <p style="color:#475569;font-size:12px;">This OTP expires in 10 minutes. If you did not request a password reset, ignore this email.</p>
                    </div>
                """.trimIndent()
                val payload = JSONObject().apply {
                    put("from", "MVCI Control <onboarding@resend.dev>")
                    put("to", JSONArray().put(recipientEmail))
                    put("subject", "MVCI Super Admin Password Reset OTP")
                    put("html", htmlBody)
                }
                val request = Request.Builder()
                    .url("https://api.resend.com/emails")
                    .addHeader("Authorization", "Bearer $resendKey")
                    .post(payload.toString().toRequestBody(JSON_MEDIA))
                    .build()
                executeHttp(request, "6-digit OTP sent to $recipientEmail.")
            }
            else -> Result.failure(
                IllegalStateException(
                    "Email OTP provider not configured. Set RESEND_API_KEY in AI Studio Secrets or configure an HTTPS OTP Webhook URL in Super Admin Settings."
                )
            )
        }
    }

    private fun executeHttp(request: Request, successMessage: String): Result<String> {
        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(successMessage)
                } else {
                    val bodyPreview = response.body?.string()?.take(180).orEmpty()
                    Result.failure(
                        IllegalStateException("Email service returned HTTP ${response.code}: $bodyPreview")
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException(e.localizedMessage ?: "Network error while sending OTP email"))
        }
    }

    companion object {
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}

class EmailOtpCoordinator(
    private val store: SuperAdminCredentialStore,
    private val transport: OtpEmailTransport = OkHttpOtpEmailTransport()
) {
    suspend fun requestOtpForRegisteredEmail(
        enteredEmail: String,
        instituteName: String = "Maa Vaishno Computer Institute",
        webhookOverride: String = store.otpWebhookUrl()
    ): Result<String> {
        val registered = store.recoveryEmail().trim().lowercase()
        val candidate = enteredEmail.trim().lowercase()
        if (candidate != registered) {
            return Result.failure(
                IllegalArgumentException("Entered email does not match registered Super Admin email ($registered).")
            )
        }
        val otp = SuperAdminCredentialManager.generateNumericOtp()
        val salt = SuperAdminCredentialManager.generateSalt()
        val hash = SuperAdminCredentialManager.sha256Hex(salt, otp)
        val expiresAt = System.currentTimeMillis() + OTP_TTL_MILLIS

        val dispatchResult = transport.sendOtpEmail(
            recipientEmail = registered,
            otpCode = otp,
            instituteName = instituteName,
            webhookOverride = webhookOverride
        )
        return dispatchResult.onSuccess {
            store.savePendingOtp(
                targetEmail = registered,
                otpSalt = salt,
                otpHash = hash,
                expiresAtMillis = expiresAt
            )
        }.onFailure {
            store.clearPendingOtp()
        }
    }

    fun verifyOtpAndResetPassword(
        otpCode: String,
        newPassword: String
    ): Result<Map<String, Any>> {
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("New password must be at least 6 characters."))
        }
        return when (val check = store.verifyPendingOtp(otpCode)) {
            SuperAdminCredentialStore.OtpCheckResult.Valid -> {
                val activeUser = store.activeUsername()
                val updateMap = SuperAdminCredentialManager.createCredentialUpdateMap(activeUser, newPassword)
                store.saveCredentialsLocal(
                    username = activeUser,
                    salt = updateMap["adminPasswordSalt"] as String,
                    hash = updateMap["adminPasswordHash"] as String,
                    recoveryEmail = store.recoveryEmail(),
                    pendingCloudSync = true
                )
                Result.success(updateMap)
            }
            SuperAdminCredentialStore.OtpCheckResult.Missing ->
                Result.failure(IllegalStateException("No active OTP found. Request a new OTP first."))
            SuperAdminCredentialStore.OtpCheckResult.Expired ->
                Result.failure(IllegalStateException("OTP expired (10 min limit). Request a new OTP."))
            SuperAdminCredentialStore.OtpCheckResult.LockedOut ->
                Result.failure(IllegalStateException("Too many wrong OTP attempts. Request a new OTP."))
            is SuperAdminCredentialStore.OtpCheckResult.Invalid ->
                Result.failure(IllegalArgumentException("Incorrect OTP (${check.remainingAttempts} attempts remaining)."))
        }
    }

    companion object {
        const val OTP_TTL_MILLIS = 10 * 60 * 1000L
    }
}

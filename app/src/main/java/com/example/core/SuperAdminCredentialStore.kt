package com.example.core

import android.content.Context
import android.content.SharedPreferences
import com.example.model.WorkspaceConfig

class SuperAdminCredentialStore(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun activeUsername(): String =
        prefs.getString(KEY_USERNAME, null)?.takeIf { it.length >= 3 }
            ?: SuperAdminCredentialManager.DEFAULT_ADMIN_USERNAME

    fun activeSalt(): String =
        prefs.getString(KEY_SALT, null)?.takeIf { it.length >= 8 }
            ?: SuperAdminCredentialManager.DEFAULT_ADMIN_SALT

    fun activePasswordHash(): String =
        prefs.getString(KEY_HASH, null)?.takeIf { it.length == 64 }
            ?: SuperAdminCredentialManager.DEFAULT_ADMIN_PASSWORD_HASH

    fun recoveryEmail(): String =
        prefs.getString(KEY_RECOVERY_EMAIL, null)?.takeIf { it.contains("@") }
            ?: SuperAdminCredentialManager.DEFAULT_RECOVERY_EMAIL

    fun otpWebhookUrl(): String =
        prefs.getString(KEY_OTP_WEBHOOK_URL, "").orEmpty()

    fun hasPendingCloudSync(): Boolean = prefs.getBoolean(KEY_PENDING_CLOUD_SYNC, false)

    fun verifyLogin(username: String, password: String): Boolean {
        val snapshot = WorkspaceConfig(
            adminUsername = activeUsername(),
            adminPasswordSalt = activeSalt(),
            adminPasswordHash = activePasswordHash(),
            recoveryEmail = recoveryEmail(),
            otpWebhookUrl = otpWebhookUrl()
        )
        return SuperAdminCredentialManager.verifyCredentials(username, password, snapshot)
    }

    fun verifyPassword(password: String): Boolean {
        val snapshot = WorkspaceConfig(
            adminUsername = activeUsername(),
            adminPasswordSalt = activeSalt(),
            adminPasswordHash = activePasswordHash(),
            recoveryEmail = recoveryEmail(),
            otpWebhookUrl = otpWebhookUrl()
        )
        return SuperAdminCredentialManager.verifyPasswordOnly(password, snapshot)
    }

    fun saveCredentialsLocal(
        username: String,
        salt: String,
        hash: String,
        recoveryEmail: String = recoveryEmail(),
        pendingCloudSync: Boolean
    ) {
        prefs.edit()
            .putString(KEY_USERNAME, username.trim())
            .putString(KEY_SALT, salt)
            .putString(KEY_HASH, hash)
            .putString(KEY_RECOVERY_EMAIL, recoveryEmail.trim())
            .putBoolean(KEY_PENDING_CLOUD_SYNC, pendingCloudSync)
            .apply()
    }

    fun mirrorFromWorkspaceConfig(config: WorkspaceConfig) {
        if (hasPendingCloudSync()) return
        prefs.edit()
            .putString(KEY_USERNAME, SuperAdminCredentialManager.resolveUsername(config))
            .putString(KEY_SALT, config.adminPasswordSalt.takeIf { it.length >= 8 } ?: SuperAdminCredentialManager.DEFAULT_ADMIN_SALT)
            .putString(KEY_HASH, config.adminPasswordHash.takeIf { it.length == 64 } ?: SuperAdminCredentialManager.DEFAULT_ADMIN_PASSWORD_HASH)
            .putString(KEY_RECOVERY_EMAIL, SuperAdminCredentialManager.resolveRecoveryEmail(config))
            .putString(KEY_OTP_WEBHOOK_URL, config.otpWebhookUrl.trim())
            .apply()
    }

    fun markCloudSynced() {
        prefs.edit().putBoolean(KEY_PENDING_CLOUD_SYNC, false).apply()
    }

    fun pendingSyncUpdateMap(): Map<String, Any> = mapOf(
        "adminUsername" to activeUsername(),
        "adminPasswordSalt" to activeSalt(),
        "adminPasswordHash" to activePasswordHash(),
        "recoveryEmail" to recoveryEmail()
    )

    fun savePendingOtp(
        targetEmail: String,
        otpSalt: String,
        otpHash: String,
        expiresAtMillis: Long
    ) {
        prefs.edit()
            .putString(KEY_OTP_EMAIL, targetEmail.trim().lowercase())
            .putString(KEY_OTP_SALT, otpSalt)
            .putString(KEY_OTP_HASH, otpHash)
            .putLong(KEY_OTP_EXPIRES_AT, expiresAtMillis)
            .putInt(KEY_OTP_ATTEMPTS, 0)
            .apply()
    }

    fun clearPendingOtp() {
        prefs.edit()
            .remove(KEY_OTP_EMAIL)
            .remove(KEY_OTP_SALT)
            .remove(KEY_OTP_HASH)
            .remove(KEY_OTP_EXPIRES_AT)
            .remove(KEY_OTP_ATTEMPTS)
            .apply()
    }

    fun verifyPendingOtp(enteredOtp: String, nowMillis: Long = System.currentTimeMillis()): OtpCheckResult {
        val storedHash = prefs.getString(KEY_OTP_HASH, null)
        val storedSalt = prefs.getString(KEY_OTP_SALT, null)
        val expiresAt = prefs.getLong(KEY_OTP_EXPIRES_AT, 0L)
        val attempts = prefs.getInt(KEY_OTP_ATTEMPTS, 0)

        if (storedHash.isNullOrBlank() || storedSalt.isNullOrBlank() || expiresAt <= 0L) {
            return OtpCheckResult.Missing
        }
        if (nowMillis > expiresAt) {
            clearPendingOtp()
            return OtpCheckResult.Expired
        }
        if (attempts >= MAX_OTP_ATTEMPTS) {
            clearPendingOtp()
            return OtpCheckResult.LockedOut
        }
        val cleanOtp = enteredOtp.trim()
        val candidateHash = SuperAdminCredentialManager.sha256Hex(storedSalt, cleanOtp)
        if (!SuperAdminCredentialManager.constantTimeEquals(storedHash, candidateHash)) {
            val nextAttempts = attempts + 1
            if (nextAttempts >= MAX_OTP_ATTEMPTS) {
                clearPendingOtp()
                return OtpCheckResult.LockedOut
            }
            prefs.edit().putInt(KEY_OTP_ATTEMPTS, nextAttempts).apply()
            return OtpCheckResult.Invalid(remainingAttempts = MAX_OTP_ATTEMPTS - nextAttempts)
        }
        clearPendingOtp()
        return OtpCheckResult.Valid
    }

    sealed class OtpCheckResult {
        data object Valid : OtpCheckResult()
        data object Missing : OtpCheckResult()
        data object Expired : OtpCheckResult()
        data object LockedOut : OtpCheckResult()
        data class Invalid(val remainingAttempts: Int) : OtpCheckResult()
    }

    companion object {
        private const val PREFS_NAME = "mvci_super_admin_auth"
        private const val KEY_USERNAME = "admin_username"
        private const val KEY_SALT = "admin_salt"
        private const val KEY_HASH = "admin_password_hash"
        private const val KEY_RECOVERY_EMAIL = "recovery_email"
        private const val KEY_OTP_WEBHOOK_URL = "otp_webhook_url"
        private const val KEY_PENDING_CLOUD_SYNC = "pending_cloud_sync"
        private const val KEY_OTP_EMAIL = "otp_target_email"
        private const val KEY_OTP_SALT = "otp_salt"
        private const val KEY_OTP_HASH = "otp_hash"
        private const val KEY_OTP_EXPIRES_AT = "otp_expires_at"
        private const val KEY_OTP_ATTEMPTS = "otp_attempts"
        const val MAX_OTP_ATTEMPTS = 5
    }
}

package com.example.core

import com.example.model.WorkspaceConfig
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom

object SuperAdminCredentialManager {
    const val DEFAULT_ADMIN_USERNAME = "Rishu@2050"
    const val DEFAULT_ADMIN_SALT = "mvci_v1_salt_2050"
    const val DEFAULT_ADMIN_PASSWORD_HASH =
        "001ea4e661e22e4d7ac2a2c46c6a5989d263afad3ac79672a184ec24dd1149d8"
    const val DEFAULT_RECOVERY_EMAIL = "rsingh13151@gmail.com"

    private val secureRandom = SecureRandom()

    fun sha256Hex(salt: String, secret: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$salt:$secret".toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun constantTimeEquals(expectedHex: String, actualHex: String): Boolean {
        return MessageDigest.isEqual(
            expectedHex.toByteArray(StandardCharsets.UTF_8),
            actualHex.toByteArray(StandardCharsets.UTF_8)
        )
    }

    fun generateSalt(): String {
        val saltBytes = ByteArray(16)
        secureRandom.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    fun generateNumericOtp(): String {
        val code = 100_000 + secureRandom.nextInt(900_000)
        return code.toString()
    }

    fun resolveUsername(config: WorkspaceConfig?): String =
        config?.adminUsername?.takeIf { it.isNotBlank() } ?: DEFAULT_ADMIN_USERNAME

    fun resolveRecoveryEmail(config: WorkspaceConfig?): String =
        config?.recoveryEmail?.takeIf { it.contains("@") } ?: DEFAULT_RECOVERY_EMAIL

    fun verifyCredentials(
        inputUsername: String,
        inputPassword: String,
        config: WorkspaceConfig?
    ): Boolean {
        val expectedUser = resolveUsername(config)
        val salt = config?.adminPasswordSalt?.takeIf { it.length >= 8 } ?: DEFAULT_ADMIN_SALT
        val expectedHash = config?.adminPasswordHash?.takeIf { it.length == 64 } ?: DEFAULT_ADMIN_PASSWORD_HASH
        val userMatches = inputUsername.trim() == expectedUser
        val candidateHash = sha256Hex(salt, inputPassword)
        return userMatches && constantTimeEquals(expectedHash, candidateHash)
    }

    fun verifyPasswordOnly(
        inputPassword: String,
        config: WorkspaceConfig?
    ): Boolean {
        val salt = config?.adminPasswordSalt?.takeIf { it.length >= 8 } ?: DEFAULT_ADMIN_SALT
        val expectedHash = config?.adminPasswordHash?.takeIf { it.length == 64 } ?: DEFAULT_ADMIN_PASSWORD_HASH
        val candidateHash = sha256Hex(salt, inputPassword)
        return constantTimeEquals(expectedHash, candidateHash)
    }

    fun createCredentialUpdateMap(newUsername: String, newPassword: String): Map<String, Any> {
        val newSalt = generateSalt()
        val newHash = sha256Hex(newSalt, newPassword)
        return mapOf(
            "adminUsername" to newUsername.trim().take(80),
            "adminPasswordSalt" to newSalt,
            "adminPasswordHash" to newHash
        )
    }

    fun defaultCredentialUpdateMap(): Map<String, Any> = mapOf(
        "adminUsername" to DEFAULT_ADMIN_USERNAME,
        "adminPasswordSalt" to DEFAULT_ADMIN_SALT,
        "adminPasswordHash" to DEFAULT_ADMIN_PASSWORD_HASH,
        "recoveryEmail" to DEFAULT_RECOVERY_EMAIL
    )
}

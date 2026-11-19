package com.example.core

object AuditSanitizer {

    private val SECRET_KEY_TOKENS = listOf(
        "password",
        "hash",
        "salt",
        "otpcode",
        "otp_code",
        "secret",
        "apikey",
        "api_key",
        "token",
        "bearer"
    )

    private val SENSITIVE_KV_REGEX = Regex(
        """(?i)\b(password|passcode|otp|otpCode|secret|token|apiKey|adminPasswordHash|adminPasswordSalt)\s*([=:])\s*([^\s,;]+)"""
    )

    fun isSensitiveKey(key: String): Boolean {
        val lower = key.lowercase()
        if (lower == "otprequired" || lower == "otpwebhookurl") return false
        return SECRET_KEY_TOKENS.any { lower.contains(it) } || lower == "otp"
    }

    fun sanitizeValue(key: String, value: Any?): String {
        if (isSensitiveKey(key)) return "[REDACTED]"
        if (value == null) return "NONE"
        val raw = when (value) {
            is Collection<*> -> value.joinToString(",", prefix = "[", postfix = "]")
            else -> value.toString()
        }
        val clean = sanitizeFreeText(raw)
        return if (clean.isBlank()) "EMPTY" else clean.take(90)
    }

    fun sanitizeFreeText(text: String, maxLen: Int = 300): String {
        val redacted = SENSITIVE_KV_REGEX.replace(text) { match ->
            "${match.groupValues[1]}${match.groupValues[2]}[REDACTED]"
        }
        return redacted.trim().take(maxLen)
    }

    fun buildSettingsDiff(
        previousData: Map<String, Any?>?,
        updates: Map<String, Any>
    ): Pair<String, String> {
        val cleanKeys = updates.keys.filter { it != "updatedAt" && it != "createdAt" }
        if (cleanKeys.isEmpty()) return "NONE" to "No setting fields modified"

        val prevParts = mutableListOf<String>()
        val nextParts = mutableListOf<String>()

        cleanKeys.forEach { key ->
            val nextVal = sanitizeValue(key, updates[key])
            val prevRaw = previousData?.get(key)
            val prevVal = if (previousData == null) "UNSPECIFIED" else sanitizeValue(key, prevRaw)
            prevParts.add("$key=$prevVal")
            nextParts.add("$key=$nextVal")
        }

        val prevJoined = prevParts.joinToString("; ").take(300)
        val nextJoined = nextParts.joinToString("; ").take(300)
        return prevJoined to nextJoined
    }

    fun classifyCategory(action: String, moduleName: String): String {
        val upperAction = action.uppercase()
        val upperModule = moduleName.uppercase()
        return when {
            upperAction.contains("PASSWORD") ||
                upperAction.contains("CREDENTIAL") ||
                upperAction.contains("OTP") ||
                upperAction.contains("LOGIN") ||
                upperAction.contains("LOCK") ||
                upperAction.contains("ROLE") ||
                upperModule.contains("SECURITY") -> "SECURITY"

            upperAction.contains("SETTING") ||
                upperAction.contains("LANGUAGE") ||
                upperAction.contains("THEME") ||
                upperAction.contains("MAINTENANCE") ||
                upperAction.contains("DATABASE_CONFIG") ||
                upperAction.contains("MODULE") ||
                upperAction.contains("DOMAIN") ||
                upperModule.contains("SYSTEM SETTINGS") -> "SETTINGS"

            upperAction.contains("FRANCHISE") || upperModule.contains("FRANCHISE") -> "FRANCHISE"
            upperAction.contains("USER") || upperAction.contains("STAFF") || upperModule.contains("USER") -> "USERS"
            upperAction.contains("CONNECT") || upperAction.contains("SYNC") || upperModule.contains("CONNECTOR") -> "CONNECTOR"
            upperAction.contains("PAYMENT") || upperModule.contains("FINANCE") -> "FINANCE"
            upperAction.contains("COURSE") ||
                upperAction.contains("EXAM") ||
                upperAction.contains("STUDENT") ||
                upperAction.contains("MARK_") ||
                upperModule.contains("ACADEMIC") ||
                upperModule.contains("ATTENDANCE") ||
                upperModule.contains("STUDENT") -> "ACADEMIC"

            else -> "SYSTEM"
        }
    }

    fun classifySeverity(action: String): String {
        val upper = action.uppercase()
        return when {
            upper.contains("PASSWORD_RESET") ||
                upper.contains("RESET_SUPER_ADMIN") ||
                upper.contains("OTP_PASSWORD_RESET") ||
                upper.contains("UPDATE_SUPER_ADMIN_CREDENTIALS") ||
                upper.contains("DELETE_") ||
                upper.contains("SUSPEND") ||
                upper.contains("PROBE_FAIL") ||
                upper.contains("DISCONNECT") -> "CRITICAL"

            upper.contains("OTP_RESET_REQUESTED") ||
                upper.contains("CRITICAL_SYSTEM_SETTING") ||
                upper.contains("MAINTENANCE") ||
                upper.contains("RECOVERY") ||
                upper.contains("ROLE") ||
                upper.contains("LOCKED") ||
                upper.contains("UPDATE_DATABASE") ||
                upper.contains("TOGGLE_OTP") ||
                upper.contains("SET_LANGUAGE") ||
                upper.contains("SET_THEME") ||
                upper.contains("QUICK_SET_") ||
                upper.contains("TOGGLE_MODULE") -> "WARNING"

            else -> "INFO"
        }
    }
}

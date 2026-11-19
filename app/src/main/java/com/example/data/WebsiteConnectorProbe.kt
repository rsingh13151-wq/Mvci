package com.example.data

import com.example.model.WebsiteProbeResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class WebsiteConnectorProbe {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    fun maskSecret(rawSecret: String): String {
        val trimmed = rawSecret.trim()
        if (trimmed.isEmpty()) return ""
        return if (trimmed.length <= 6) {
            "••••${trimmed.takeLast(2)}"
        } else {
            "${trimmed.take(4)}••••${trimmed.takeLast(4)}"
        }
    }

    suspend fun probeUrl(
        rawUrl: String,
        bearerToken: String = "",
        apiKeyHeaderName: String = "",
        apiKeyValue: String = ""
    ): WebsiteProbeResult = withContext(Dispatchers.IO) {
        val normalized = normalizeUrl(rawUrl)
        if (normalized == null) {
            return@withContext WebsiteProbeResult(
                targetUrl = rawUrl,
                reachable = false,
                httpCode = 0,
                sslValid = false,
                tlsProtocol = "N/A",
                latencyMs = 0L,
                serverHeader = "",
                contentType = "",
                message = "Invalid URL format. Use https://domain.com"
            )
        }

        val start = System.currentTimeMillis()
        try {
            val builder = Request.Builder()
                .url(normalized)
                .get()
                .header("User-Agent", "MVCI-SuperAdmin-Connector/1.0")
            if (bearerToken.isNotBlank()) {
                builder.header("Authorization", "Bearer ${bearerToken.trim()}")
            }
            if (apiKeyHeaderName.isNotBlank() && apiKeyValue.isNotBlank()) {
                builder.header(apiKeyHeaderName.trim(), apiKeyValue.trim())
            }

            client.newCall(builder.build()).execute().use { response ->
                val latency = (System.currentTimeMillis() - start).coerceAtLeast(1L)
                val tls = response.handshake?.tlsVersion?.javaName ?: if (normalized.startsWith("https://")) "TLS" else "HTTP"
                val sslOk = normalized.startsWith("https://") && response.handshake != null
                val server = response.header("Server") ?: "Unknown"
                val contentType = response.header("Content-Type") ?: "text/html"
                val code = response.code
                val ok = code in 200..399
                val msg = when {
                    ok -> "Verified HTTP $code ($tls) in ${latency}ms"
                    code == 401 || code == 403 -> "Authentication required (HTTP $code). Provide valid API credentials."
                    else -> "Responded with HTTP $code in ${latency}ms"
                }
                WebsiteProbeResult(
                    targetUrl = normalized,
                    reachable = ok,
                    httpCode = code,
                    sslValid = sslOk,
                    tlsProtocol = tls,
                    latencyMs = latency,
                    serverHeader = server,
                    contentType = contentType,
                    message = msg
                )
            }
        } catch (e: Exception) {
            val latency = (System.currentTimeMillis() - start).coerceAtLeast(1L)
            WebsiteProbeResult(
                targetUrl = normalized,
                reachable = false,
                httpCode = 0,
                sslValid = false,
                tlsProtocol = "None",
                latencyMs = latency,
                serverHeader = "",
                contentType = "",
                message = e.localizedMessage ?: "Connection failed"
            )
        }
    }

    private fun normalizeUrl(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
        return if (withScheme.contains(".") && !withScheme.contains(" ")) withScheme else null
    }
}

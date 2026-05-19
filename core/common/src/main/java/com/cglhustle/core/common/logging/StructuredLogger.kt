package com.cglhustle.core.common.logging

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}

interface StructuredLogger {
    fun log(
        level: LogLevel,
        module: String,
        event: String,
        correlationId: String? = null,
        payload: String? = null,
        throwable: Throwable? = null
    )
}

@Singleton
class LogcatStructuredLogger @Inject constructor() : StructuredLogger {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

    override fun log(
        level: LogLevel,
        module: String,
        event: String,
        correlationId: String?,
        payload: String?,
        throwable: Throwable?
    ) {
        val timestamp = dateFormat.format(Date(System.currentTimeMillis()))

        val maskedPayload = payload?.let { maskPii(it) }

        val logMessageBuilder = java.lang.StringBuilder()
            .append("[$timestamp] ")
            .append("[$module] ")
            .append("Event: $event")

        if (correlationId != null) {
            logMessageBuilder.append(" | CorrelationId: $correlationId")
        }

        if (maskedPayload != null) {
            logMessageBuilder.append(" | Payload: $maskedPayload")
        }

        val logMessage = logMessageBuilder.toString()
        val tag = "CGL_Hustle_$module"

        when (level) {
            LogLevel.DEBUG -> Log.d(tag, logMessage, throwable)
            LogLevel.INFO -> Log.i(tag, logMessage, throwable)
            LogLevel.WARN -> Log.w(tag, logMessage, throwable)
            LogLevel.ERROR -> Log.e(tag, logMessage, throwable)
        }
    }

    private fun maskPii(jsonPayload: String): String {
        var masked = jsonPayload
        val keysToMask = listOf("userId", "user_id", "email", "password", "token", "idempotencyKey")

        for (key in keysToMask) {
            try {
                // Regex to match "key": "value", "key": 123, "key": null, etc. in a JSON-like string
                // We'll replace the value part with "[REDACTED]"
                // This regex handles double quotes around the key, and varying spaces
                val regex = Regex("(\"$key\"\\s*:\\s*)(?:\"[^\"]*\"|[^,}]+)")
                masked = masked.replace(regex, "$1\"[REDACTED]\"")
            } catch (e: Exception) {
                // Ignore exception and continue masking other keys
            }
        }
        return masked
    }
}

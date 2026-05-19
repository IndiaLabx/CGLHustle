package com.cglhustle.core.common.logging

import org.junit.Assert.assertTrue
import org.junit.Test

class StructuredLoggerTest {

    // Helper method to access private maskPii method for testing
    private fun maskPii(payload: String): String {
        val logger = LogcatStructuredLogger()
        val method = logger.javaClass.getDeclaredMethod("maskPii", String::class.java)
        method.isAccessible = true
        return method.invoke(logger, payload) as String
    }

    @Test
    fun testMaskingPiiKeys() {
        val payload = """
            {
                "userId": "123-abc",
                "user_id": "456-def",
                "email": "test@example.com",
                "password": "secret_password",
                "token": "bearer_123",
                "idempotencyKey": "key_789",
                "safeKey": "safe_value"
            }
        """.trimIndent()

        val masked = maskPii(payload)

        assertTrue(masked.contains("\"userId\": \"[REDACTED]\""))
        assertTrue(masked.contains("\"user_id\": \"[REDACTED]\""))
        assertTrue(masked.contains("\"email\": \"[REDACTED]\""))
        assertTrue(masked.contains("\"password\": \"[REDACTED]\""))
        assertTrue(masked.contains("\"token\": \"[REDACTED]\""))
        assertTrue(masked.contains("\"idempotencyKey\": \"[REDACTED]\""))
        assertTrue(masked.contains("\"safeKey\": \"safe_value\""))
    }

    @Test
    fun testMaskingNonStringValues() {
        val payload = """{"user_id": 12345, "token": null, "safe": true}"""
        val masked = maskPii(payload)

        assertTrue(masked.contains("\"user_id\": \"[REDACTED]\""))
        assertTrue(masked.contains("\"token\": \"[REDACTED]\""))
        assertTrue(masked.contains("\"safe\": true"))
    }
}

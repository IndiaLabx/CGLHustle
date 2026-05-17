package com.cglhustle.core.database.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.regex.Pattern

class IdValidationTest {
    // Basic UUID v4 regex
    private val uuidRegex = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    // ULID regex: 26 chars, Crockford Base32
    private val ulidRegex = Pattern.compile("^[0-7][0-9A-HJKMNP-TV-Z]{25}$")

    fun isValidUUID(id: String): Boolean {
        return uuidRegex.matcher(id).matches()
    }

    fun isValidULID(id: String): Boolean {
        return ulidRegex.matcher(id).matches()
    }

    @Test
    fun testUUIDValidation() {
        assertTrue(isValidUUID("123e4567-e89b-12d3-a456-426614174000"))
        assertFalse(isValidUUID("invalid-uuid"))
        assertFalse(isValidUUID("123e4567-e89b-12d3-a456-42661417400G")) // Invalid char
    }

    @Test
    fun testULIDValidation() {
        assertTrue(isValidULID("01ARZ3NDEKTSV4RRFFQ69G5FAV"))
        assertFalse(isValidULID("01ARZ3NDEKTSV4RRFFQ69G5FA")) // Too short
        assertFalse(isValidULID("01ARZ3NDEKTSV4RRFFQ69G5FAU")) // Invalid char 'U'
        assertFalse(isValidULID("81ARZ3NDEKTSV4RRFFQ69G5FAV")) // Invalid first char '8'
    }
}

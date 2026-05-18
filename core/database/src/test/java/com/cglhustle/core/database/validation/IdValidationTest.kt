package com.cglhustle.core.database.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IdValidationTest {
    @Test
    fun testUUIDValidation() {
        assertTrue(IdValidator.isValidUUID("123e4567-e89b-12d3-a456-426614174000"))
        assertFalse(IdValidator.isValidUUID("invalid-uuid"))
        assertFalse(IdValidator.isValidUUID("123e4567-e89b-12d3-a456-42661417400G")) // Invalid char
    }

    @Test
    fun testULIDValidation() {
        assertTrue(IdValidator.isValidULID("01ARZ3NDEKTSV4RRFFQ69G5FAV"))
        assertFalse(IdValidator.isValidULID("01ARZ3NDEKTSV4RRFFQ69G5FA")) // Too short
        assertFalse(IdValidator.isValidULID("01ARZ3NDEKTSV4RRFFQ69G5FAU")) // Invalid char 'U'
        assertFalse(IdValidator.isValidULID("81ARZ3NDEKTSV4RRFFQ69G5FAV")) // Invalid first char '8'
    }

    @Test
    fun testValidateResult() {
        val validUUID = IdValidator.validateUUID("123e4567-e89b-12d3-a456-426614174000")
        assertTrue(validUUID.isSuccess)

        val invalidUUID = IdValidator.validateUUID("invalid-uuid")
        assertTrue(invalidUUID.isFailure)
        assertTrue(invalidUUID.exceptionOrNull() is IllegalArgumentException)

        val validULID = IdValidator.validateULID("01ARZ3NDEKTSV4RRFFQ69G5FAV")
        assertTrue(validULID.isSuccess)

        val invalidULID = IdValidator.validateULID("invalid-ulid")
        assertTrue(invalidULID.isFailure)
        assertTrue(invalidULID.exceptionOrNull() is IllegalArgumentException)
    }
}

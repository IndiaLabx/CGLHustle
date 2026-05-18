package com.cglhustle.core.database.validation

import java.util.regex.Pattern

object IdValidator {
    private val UUID_REGEX = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    // ULID Crockford Base32, uppercase only (excluding I, L, O, U)
    private val ULID_REGEX = Pattern.compile("^[0-7][0-9A-HJKMNP-TV-Z]{25}$")

    fun isValidUUID(id: String): Boolean {
        return UUID_REGEX.matcher(id).matches()
    }

    fun isValidULID(id: String): Boolean {
        return ULID_REGEX.matcher(id).matches()
    }

    fun validateUUID(id: String): Result<String> {
        return if (isValidUUID(id)) {
            Result.success(id)
        } else {
            Result.failure(IllegalArgumentException("Invalid UUID format: \$id"))
        }
    }

    fun validateULID(id: String): Result<String> {
        return if (isValidULID(id)) {
            Result.success(id)
        } else {
            Result.failure(IllegalArgumentException("Invalid ULID format: \$id"))
        }
    }
}

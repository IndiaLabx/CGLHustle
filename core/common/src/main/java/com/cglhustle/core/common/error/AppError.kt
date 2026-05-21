package com.cglhustle.core.common.error

sealed interface AppError {
    val telemetryCode: String
    val recoveryAction: RecoveryAction
}

sealed interface NetworkError : AppError {

    data class Conflict(
        override val telemetryCode: String = "ERR_NET_CONFLICT",
        override val recoveryAction: RecoveryAction = RecoveryAction.RETRY_SILENTLY
    ) : NetworkError


    data class Transient(
        override val telemetryCode: String = "ERR_NET_TRANSIENT",
        override val recoveryAction: RecoveryAction = RecoveryAction.RETRY_SILENTLY
    ) : NetworkError

    data class AuthExpired(
        override val telemetryCode: String = "ERR_NET_AUTH",
        override val recoveryAction: RecoveryAction = RecoveryAction.PROMPT_LOGIN
    ) : NetworkError

    data class NotFound(
        override val telemetryCode: String = "ERR_NET_NOT_FOUND",
        override val recoveryAction: RecoveryAction = RecoveryAction.SHOW_TOAST
    ) : NetworkError

    data class ServerOutage(
        override val telemetryCode: String = "ERR_NET_SERVER_OUTAGE",
        override val recoveryAction: RecoveryAction = RecoveryAction.RETRY_SILENTLY
    ) : NetworkError
}

sealed interface StorageError : AppError {

    data class DiskFull(
        override val telemetryCode: String = "ERR_DB_FULL",
        override val recoveryAction: RecoveryAction = RecoveryAction.FATAL_HALT
    ) : StorageError

    data class DatabaseCorruption(
        override val telemetryCode: String = "ERR_DB_CORRUPT",
        override val recoveryAction: RecoveryAction = RecoveryAction.FATAL_HALT
    ) : StorageError

    data class ConstraintViolation(
        override val telemetryCode: String = "ERR_DB_CONSTRAINT",
        override val recoveryAction: RecoveryAction = RecoveryAction.FATAL_HALT
    ) : StorageError
}

sealed interface ValidationError : AppError {

    data class MalformedId(
        override val telemetryCode: String = "ERR_VAL_MALFORMED_ID",
        override val recoveryAction: RecoveryAction = RecoveryAction.FATAL_HALT
    ) : ValidationError

    data class InvalidPayload(
        override val telemetryCode: String = "ERR_VAL_INVALID_PAYLOAD",
        override val recoveryAction: RecoveryAction = RecoveryAction.FATAL_HALT
    ) : ValidationError
}

data class UnknownError(
    val exception: Throwable? = null,
    override val telemetryCode: String = "ERR_UNKNOWN",
    override val recoveryAction: RecoveryAction = RecoveryAction.FATAL_HALT
) : AppError

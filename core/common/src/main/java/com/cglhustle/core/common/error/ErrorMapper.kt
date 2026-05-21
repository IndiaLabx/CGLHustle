package com.cglhustle.core.common.error

import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteDiskIOException
import android.database.sqlite.SQLiteFullException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.UnknownHostException

fun Throwable.toAppError(): AppError {
    return when (this) {
        // Network
        is ConnectTimeoutException,
        is HttpRequestTimeoutException,
        is SocketTimeoutException,
        is UnknownHostException,
        is IOException -> NetworkError.Transient()
        is ResponseException -> {
            when (response.status.value) {
                401 -> NetworkError.AuthExpired()
                404 -> NetworkError.NotFound()
                409 -> NetworkError.Conflict()
                in 500..599 -> NetworkError.ServerOutage()
                else -> NetworkError.Transient()
            }
        }
        // Storage
        is SQLiteConstraintException -> StorageError.ConstraintViolation()
        is SQLiteDiskIOException,
        is SQLiteFullException -> StorageError.DiskFull()
        is SQLiteDatabaseCorruptException -> StorageError.DatabaseCorruption()
        // Validation
        is SerializationException -> ValidationError.InvalidPayload()
        // Unknown
        else -> UnknownError(this)
    }
}

/**
 * Extension function to map internal technical AppErrors to non-technical,
 * user-friendly English strings suitable for UI display.
 */
fun AppError.toUserFriendlyMessage(): String {
    return when (this) {
        is NetworkError.Conflict -> "We encountered a conflict syncing your data."
        is NetworkError.Transient -> "We're having trouble reaching the network. Please check your connection."
        is NetworkError.AuthExpired -> "Your session has expired. Please log in again to sync your progress."
        is NetworkError.NotFound -> "We couldn't find what you were looking for."
        is NetworkError.ServerOutage -> "Our servers are taking a short break. We will save your progress offline."

        is StorageError.DiskFull -> "Your device is out of storage space. Please free up some space to continue."
        is StorageError.DatabaseCorruption -> "There is a problem with your local data. Please reinstall the app."
        is StorageError.ConstraintViolation -> "An unexpected local data constraint was violated."

        is ValidationError.MalformedId -> "An invalid identifier was detected."
        is ValidationError.InvalidPayload -> "The data provided is invalid."

        is UnknownError -> "An unexpected error occurred. Please try again later."
    }
}

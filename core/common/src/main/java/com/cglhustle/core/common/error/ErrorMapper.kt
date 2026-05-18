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
        // Network: Transient
        is ConnectTimeoutException,
        is HttpRequestTimeoutException,
        is SocketTimeoutException,
        is UnknownHostException,
        is IOException -> NetworkError.Transient()

        // Network: Specific HTTP responses
        is ResponseException -> {
            when (this.response.status.value) {
                401 -> NetworkError.AuthExpired()
                404 -> NetworkError.NotFound()
                in 500..599 -> NetworkError.ServerOutage()
                else -> NetworkError.Transient()
            }
        }

        // Storage: SQLite Exceptions
        is SQLiteConstraintException -> StorageError.ConstraintViolation()
        is SQLiteDiskIOException,
        is SQLiteFullException -> StorageError.DiskFull()
        is SQLiteDatabaseCorruptException -> StorageError.DatabaseCorruption()

        // Validation
        is SerializationException -> ValidationError.InvalidPayload()

        // Fallback
        else -> UnknownError(this)
    }
}

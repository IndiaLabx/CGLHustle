package com.cglhustle.core.common.error

import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteDiskIOException
import android.database.sqlite.SQLiteFullException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.io.IOException
import java.net.UnknownHostException

// A simple manual subclass to override response status
class FakeResponseException(response: HttpResponse, cachedResponseText: String) : ResponseException(response, cachedResponseText)

class ErrorMapperTest {

    @Test
    fun `map ConnectTimeoutException to NetworkError Transient`() {
        val error = ConnectTimeoutException("timeout", null).toAppError()
        assertTrue(error is NetworkError.Transient)
        assertEquals("ERR_NET_TRANSIENT", error.telemetryCode)
        assertEquals(RecoveryAction.RETRY_SILENTLY, error.recoveryAction)
    }

    @Test
    fun `map HttpRequestTimeoutException to NetworkError Transient`() {
        val error = HttpRequestTimeoutException("url", 1000L).toAppError()
        assertTrue(error is NetworkError.Transient)
        assertEquals("ERR_NET_TRANSIENT", error.telemetryCode)
        assertEquals(RecoveryAction.RETRY_SILENTLY, error.recoveryAction)
    }

    @Test
    fun `map SocketTimeoutException to NetworkError Transient`() {
        val error = SocketTimeoutException("timeout", null).toAppError()
        assertTrue(error is NetworkError.Transient)
        assertEquals("ERR_NET_TRANSIENT", error.telemetryCode)
    }

    @Test
    fun `map UnknownHostException to NetworkError Transient`() {
        val error = UnknownHostException("host").toAppError()
        assertTrue(error is NetworkError.Transient)
        assertEquals("ERR_NET_TRANSIENT", error.telemetryCode)
    }

    @Test
    fun `map generic IOException to NetworkError Transient`() {
        val error = IOException("io error").toAppError()
        assertTrue(error is NetworkError.Transient)
        assertEquals("ERR_NET_TRANSIENT", error.telemetryCode)
    }

    @Test
    fun `map ResponseException 401 to NetworkError AuthExpired`() {
        val mockResponse = mock(HttpResponse::class.java)
        `when`(mockResponse.status).thenReturn(HttpStatusCode.Unauthorized)
        val exception = FakeResponseException(mockResponse, "Error")
        val error = exception.toAppError()
        assertTrue(error is NetworkError.AuthExpired)
        assertEquals("ERR_NET_AUTH", error.telemetryCode)
        assertEquals(RecoveryAction.PROMPT_LOGIN, error.recoveryAction)
    }

    @Test
    fun `map ResponseException 404 to NetworkError NotFound`() {
        val mockResponse = mock(HttpResponse::class.java)
        `when`(mockResponse.status).thenReturn(HttpStatusCode.NotFound)
        val exception = FakeResponseException(mockResponse, "Error")
        val error = exception.toAppError()
        assertTrue(error is NetworkError.NotFound)
        assertEquals("ERR_NET_NOT_FOUND", error.telemetryCode)
        assertEquals(RecoveryAction.SHOW_TOAST, error.recoveryAction)
    }

    @Test
    fun `map ResponseException other 4xx to NetworkError Transient`() {
        val mockResponse = mock(HttpResponse::class.java)
        `when`(mockResponse.status).thenReturn(HttpStatusCode.BadRequest)
        val exception = FakeResponseException(mockResponse, "Error")
        val error = exception.toAppError()
        assertTrue(error is NetworkError.Transient)
        assertEquals("ERR_NET_TRANSIENT", error.telemetryCode)
    }

    @Test
    fun `map ResponseException 5xx to NetworkError ServerOutage`() {
        val mockResponse = mock(HttpResponse::class.java)
        `when`(mockResponse.status).thenReturn(HttpStatusCode.InternalServerError)
        val exception = FakeResponseException(mockResponse, "Error")
        val error = exception.toAppError()
        assertTrue(error is NetworkError.ServerOutage)
        assertEquals("ERR_NET_SERVER_OUTAGE", error.telemetryCode)
        assertEquals(RecoveryAction.RETRY_SILENTLY, error.recoveryAction)
    }

    @Test
    fun `map SQLiteConstraintException to StorageError ConstraintViolation`() {
        val error = SQLiteConstraintException().toAppError()
        assertTrue(error is StorageError.ConstraintViolation)
        assertEquals("ERR_DB_CONSTRAINT", error.telemetryCode)
        assertEquals(RecoveryAction.FATAL_HALT, error.recoveryAction)
    }

    @Test
    fun `map SQLiteDiskIOException to StorageError DiskFull`() {
        val error = SQLiteDiskIOException().toAppError()
        assertTrue(error is StorageError.DiskFull)
        assertEquals("ERR_DB_FULL", error.telemetryCode)
        assertEquals(RecoveryAction.FATAL_HALT, error.recoveryAction)
    }

    @Test
    fun `map SQLiteFullException to StorageError DiskFull`() {
        val error = SQLiteFullException().toAppError()
        assertTrue(error is StorageError.DiskFull)
        assertEquals("ERR_DB_FULL", error.telemetryCode)
    }

    @Test
    fun `map SQLiteDatabaseCorruptException to StorageError DatabaseCorruption`() {
        val error = SQLiteDatabaseCorruptException().toAppError()
        assertTrue(error is StorageError.DatabaseCorruption)
        assertEquals("ERR_DB_CORRUPT", error.telemetryCode)
        assertEquals(RecoveryAction.FATAL_HALT, error.recoveryAction)
    }

    @Test
    fun `map SerializationException to ValidationError InvalidPayload`() {
        val error = SerializationException("Invalid json").toAppError()
        assertTrue(error is ValidationError.InvalidPayload)
        assertEquals("ERR_VAL_INVALID_PAYLOAD", error.telemetryCode)
        assertEquals(RecoveryAction.FATAL_HALT, error.recoveryAction)
    }

    @Test
    fun `map Unknown Throwable to UnknownError`() {
        val exception = RuntimeException("Something went wrong")
        val error = exception.toAppError()
        assertTrue(error is UnknownError)
        assertEquals("ERR_UNKNOWN", error.telemetryCode)
        assertEquals(RecoveryAction.FATAL_HALT, error.recoveryAction)
        assertEquals(exception, (error as UnknownError).exception)
    }
}

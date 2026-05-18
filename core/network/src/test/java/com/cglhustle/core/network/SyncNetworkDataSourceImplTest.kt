package com.cglhustle.core.network

import com.cglhustle.core.common.error.AppResult
import com.cglhustle.core.common.error.Failure
import com.cglhustle.core.common.error.NetworkError
import com.cglhustle.core.common.error.Success
import com.cglhustle.core.database.entity.SyncEventEntity
import com.cglhustle.core.database.entity.SyncEventType
import com.cglhustle.core.database.entity.SyncStatus
import com.cglhustle.core.network.dto.UserAnswerDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.ConnectException

class SyncNetworkDataSourceImplTest {

    private lateinit var json: Json
    private lateinit var dummyEvent: SyncEventEntity
    private lateinit var userAnswerDto: UserAnswerDto
    private lateinit var payloadJson: String

    @Before
    fun setup() {
        json = Json { ignoreUnknownKeys = true; prettyPrint = true; isLenient = true }
        userAnswerDto = UserAnswerDto(
            id = "id_123",
            sessionId = "sess_123",
            questionId = "q_123",
            selectedOptionId = "opt_123",
            isCorrect = true,
            metadata = "{}",
            createdAt = 123456789L
        )
        payloadJson = json.encodeToString(userAnswerDto)
        dummyEvent = SyncEventEntity(
            id = 1L,
            userId = "user_123",
            idempotencyKey = "key_123",
            eventType = SyncEventType.UPSERT_ANSWER,
            payload = payloadJson,
            status = SyncStatus.PENDING,
            createdAt = 123456789L,
            nextRetryAt = null,
            retryCount = 0,
            lastErrorCode = null,
            lastErrorAt = null
        )
    }

    private fun createDataSource(engine: MockEngine): SyncNetworkDataSourceImpl {
        val client = HttpClient(engine) {
            install(ContentNegotiation) {
                json(json)
            }
            expectSuccess = true
        }
        return SyncNetworkDataSourceImpl(client)
    }

    @Test
    fun `pushEvent returns Success when response is 200 OK`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = """{"status": "ok"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val dataSource = createDataSource(engine)

        val result = dataSource.pushEvent(dummyEvent)

        assertTrue(result is Success)
    }

    @Test
    fun `pushEvent returns Failure(AuthExpired) when response is 401 Unauthorized`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = "Unauthorized",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "text/plain")
            )
        }
        val dataSource = createDataSource(engine)

        val result = dataSource.pushEvent(dummyEvent)

        assertTrue(result is Failure)
        assertTrue((result as Failure).error is NetworkError.AuthExpired)
    }

    @Test
    fun `pushEvent returns Failure(Transient) when exception is thrown`() = runTest {
        val engine = MockEngine { _ ->
            throw ConnectException("Connection refused")
        }
        val dataSource = createDataSource(engine)

        val result = dataSource.pushEvent(dummyEvent)

        assertTrue(result is Failure)
        assertTrue((result as Failure).error is NetworkError.Transient)
    }
}

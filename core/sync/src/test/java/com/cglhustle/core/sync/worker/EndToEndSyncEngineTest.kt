package com.cglhustle.core.sync.worker

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.CoroutineWorker
import com.cglhustle.core.database.CglHustleDatabase
import com.cglhustle.core.database.dao.QuizSessionDao
import com.cglhustle.core.database.dao.SyncEventDao
import com.cglhustle.core.database.dao.UserAnswerDao
import com.cglhustle.core.database.entity.AnswerMutationType
import com.cglhustle.core.database.entity.QuizSessionEntity
import com.cglhustle.core.database.entity.SessionStatus
import com.cglhustle.core.database.entity.SyncEventEntity
import com.cglhustle.core.database.entity.SyncEventType
import com.cglhustle.core.database.entity.SyncStatus
import com.cglhustle.core.database.entity.UserAnswerEntity
import com.cglhustle.core.network.SyncNetworkDataSourceImpl
import com.cglhustle.core.network.dto.UserAnswerDto
import com.cglhustle.core.sync.orchestrator.SyncOrchestrator
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.mockito.Mockito.mock

@RunWith(RobolectricTestRunner::class)
class EndToEndSyncEngineTest {

    private lateinit var context: Context
    private lateinit var database: CglHustleDatabase
    private lateinit var syncEventDao: SyncEventDao
    private lateinit var quizSessionDao: QuizSessionDao
    private lateinit var userAnswerDao: UserAnswerDao
    private lateinit var workManager: WorkManager
    private lateinit var syncOrchestrator: SyncOrchestrator

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; prettyPrint = true }

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        workManager = WorkManager.getInstance(context)

        database = Room.inMemoryDatabaseBuilder(
            context, CglHustleDatabase::class.java
        ).allowMainThreadQueries().setTransactionExecutor(java.util.concurrent.Executors.newSingleThreadExecutor()).setQueryExecutor(java.util.concurrent.Executors.newSingleThreadExecutor()).build()

        syncEventDao = database.syncEventDao()
        quizSessionDao = database.quizSessionDao()
        userAnswerDao = database.userAnswerDao()

        syncOrchestrator = mock(SyncOrchestrator::class.java)
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun createMockKtorEngine(status: HttpStatusCode, responseBody: String = ""): HttpClient {
        val mockEngine = MockEngine { _ ->
            respond(
                content = responseBody,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
            // Ktor does not throw exceptions on bad status codes by default unless expectSuccess is true
            expectSuccess = true
        }
    }

    @Test
    fun `Full-chain E2E Success Path`() = runTest {
        // Step A & B done in setup and inside the test execution

        // Step C: Trigger a local atomic transaction write
        val sessionId = "session_123"
        val userId = "user_123"
        val questionId = "question_123"
        val eventId = "EVENT_01"
        val idempotencyKey = "IDEM_01"

        val session = QuizSessionEntity(
            sessionId = sessionId, userId = userId, quizMetadataId = "quiz_1", status = SessionStatus.IN_PROGRESS,
            startTime = System.currentTimeMillis(), lastPausedTime = null, endTime = null, totalPausedDurationMs = 0L,
            activeDurationMs = 0L, currentQuestionId = questionId, sessionVersion = 1, lastMutationId = eventId,
            idempotencyKey = "S_IDEM_01", deviceFingerprint = null, clientGeneratedAt = System.currentTimeMillis(),
            serverReceivedAt = null, updatedAt = System.currentTimeMillis()
        )
        quizSessionDao.insertSession(session)

        val answer = UserAnswerEntity(
            eventId = eventId, supersedesEventId = null, mutationType = AnswerMutationType.SELECT,
            sessionId = sessionId, userId = userId, questionId = questionId, selectedOption = "option_a",
            isCorrect = true, timeTakenSeconds = 1.5, attemptSequence = 1, idempotencyKey = idempotencyKey,
            clientGeneratedAt = System.currentTimeMillis(), serverReceivedAt = null, updatedAt = System.currentTimeMillis()
        )

        val answerDto = UserAnswerDto(
            id = eventId, sessionId = sessionId, questionId = questionId, selectedOptionId = "option_a",
            isCorrect = true, metadata = "{}", createdAt = System.currentTimeMillis()
        )

        val payloadJson = json.encodeToString(answerDto)

        val syncEvent = SyncEventEntity(
            userId = userId, idempotencyKey = idempotencyKey, eventType = SyncEventType.UPSERT_ANSWER,
            payload = payloadJson, status = SyncStatus.PENDING, createdAt = System.currentTimeMillis(),
            nextRetryAt = null, retryCount = 0, lastErrorCode = null, lastErrorAt = null
        )

        userAnswerDao.saveAnswerWithOutbox(answer, syncEvent, System.currentTimeMillis())

        val pendingEvents = syncEventDao.getPendingEvents()
        assertEquals(1, pendingEvents.size)
        val insertedEventId = pendingEvents[0].id

        // Step D: Programmatically execute OutboxSyncWorker via WorkManager's testing framework
        val mockHttpClient = createMockKtorEngine(HttpStatusCode.OK)
        val syncNetworkDataSource = SyncNetworkDataSourceImpl(mockHttpClient)

        val worker = TestListenableWorkerBuilder<OutboxSyncWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters
                ): ListenableWorker {
                    return OutboxSyncWorker(appContext, workerParameters, syncEventDao, syncNetworkDataSource, syncOrchestrator)
                }
            })
            .build()

        val result = (worker as CoroutineWorker).doWork()

        // Step E: Assert the entire chain
        assertEquals(ListenableWorker.Result.success(), result)

        // Status should be ACKED
        val ackedEvents = syncEventDao.getPendingEvents(listOf(SyncStatus.ACKED))
        val updatedEvent = ackedEvents.find { it.id == insertedEventId }
        assertNotNull(updatedEvent)
        assertEquals(SyncStatus.ACKED, updatedEvent?.status)
        assertNull(updatedEvent?.processingToken)
        assertNotNull(updatedEvent?.lastAttemptAt)
    }

    @Test
    fun `Full-chain E2E 503 Server Outage Translation`() = runTest {
        val sessionId = "session_123"
        val userId = "user_123"
        val questionId = "question_123"
        val eventId = "EVENT_02"
        val idempotencyKey = "IDEM_02"

        val session = QuizSessionEntity(
            sessionId = sessionId, userId = userId, quizMetadataId = "quiz_1", status = SessionStatus.IN_PROGRESS,
            startTime = System.currentTimeMillis(), lastPausedTime = null, endTime = null, totalPausedDurationMs = 0L,
            activeDurationMs = 0L, currentQuestionId = questionId, sessionVersion = 1, lastMutationId = eventId,
            idempotencyKey = "S_IDEM_02", deviceFingerprint = null, clientGeneratedAt = System.currentTimeMillis(),
            serverReceivedAt = null, updatedAt = System.currentTimeMillis()
        )
        quizSessionDao.insertSession(session)

        val answer = UserAnswerEntity(
            eventId = eventId, supersedesEventId = null, mutationType = AnswerMutationType.SELECT,
            sessionId = sessionId, userId = userId, questionId = questionId, selectedOption = "option_b",
            isCorrect = false, timeTakenSeconds = 2.0, attemptSequence = 2, idempotencyKey = idempotencyKey,
            clientGeneratedAt = System.currentTimeMillis(), serverReceivedAt = null, updatedAt = System.currentTimeMillis()
        )

        val answerDto = UserAnswerDto(
            id = eventId, sessionId = sessionId, questionId = questionId, selectedOptionId = "option_b",
            isCorrect = false, metadata = "{}", createdAt = System.currentTimeMillis()
        )

        val syncEvent = SyncEventEntity(
            userId = userId, idempotencyKey = idempotencyKey, eventType = SyncEventType.UPSERT_ANSWER,
            payload = json.encodeToString(answerDto), status = SyncStatus.PENDING, createdAt = System.currentTimeMillis(),
            nextRetryAt = null, retryCount = 0, lastErrorCode = null, lastErrorAt = null
        )

        userAnswerDao.saveAnswerWithOutbox(answer, syncEvent, System.currentTimeMillis())
        val pendingEvents = syncEventDao.getPendingEvents()
        val insertedEventId = pendingEvents[0].id

        // Mock Ktor 503 Server Outage
        val mockHttpClient = createMockKtorEngine(HttpStatusCode.ServiceUnavailable)
        val syncNetworkDataSource = SyncNetworkDataSourceImpl(mockHttpClient)

        val worker = TestListenableWorkerBuilder<OutboxSyncWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters
                ): ListenableWorker {
                    return OutboxSyncWorker(appContext, workerParameters, syncEventDao, syncNetworkDataSource, syncOrchestrator)
                }
            })
            .build()

        val result = (worker as CoroutineWorker).doWork()

        // Worker translates it to retry
        assertEquals(ListenableWorker.Result.retry(), result)

        // State should be FAILED_RETRY
        val failedEvents = syncEventDao.getPendingEvents(listOf(SyncStatus.FAILED_RETRY))
        val updatedEvent = failedEvents.find { it.id == insertedEventId }
        assertNotNull(updatedEvent)
        assertEquals(SyncStatus.FAILED_RETRY, updatedEvent?.status)
        assertNull(updatedEvent?.processingToken)
        assertNotNull(updatedEvent?.lastAttemptAt)
    }
}

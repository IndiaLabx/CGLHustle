package com.cglhustle.core.database.dao

import android.content.Context
import android.database.SQLException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.room.withTransaction
import com.cglhustle.core.database.CglHustleDatabase
import com.cglhustle.core.database.entity.AnswerMutationType
import com.cglhustle.core.database.entity.QuizSessionEntity
import com.cglhustle.core.database.entity.SessionStatus
import com.cglhustle.core.database.entity.SyncEventEntity
import com.cglhustle.core.database.entity.SyncEventType
import com.cglhustle.core.database.entity.SyncStatus
import com.cglhustle.core.database.entity.UserAnswerEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class UserAnswerDaoTest {
    private lateinit var db: CglHustleDatabase
    private lateinit var userAnswerDao: UserAnswerDao
    private lateinit var quizSessionDao: QuizSessionDao
    private lateinit var syncEventDao: SyncEventDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, CglHustleDatabase::class.java
        ).allowMainThreadQueries().build()
        userAnswerDao = db.userAnswerDao()
        quizSessionDao = db.quizSessionDao()
        syncEventDao = db.syncEventDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun saveAnswerWithOutbox_atomicSuccess(): Unit = runBlocking {
        val sessionId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()

        val session = QuizSessionEntity(
            sessionId = sessionId,
            userId = userId,
            quizMetadataId = UUID.randomUUID().toString(),
            status = SessionStatus.IN_PROGRESS,
            startTime = System.currentTimeMillis(),
            lastPausedTime = null,
            endTime = null,
            totalPausedDurationMs = 0,
            activeDurationMs = 0,
            currentQuestionId = null,
            sessionVersion = 1,
            lastMutationId = UUID.randomUUID().toString(),
            idempotencyKey = UUID.randomUUID().toString(),
            deviceFingerprint = null,
            clientGeneratedAt = System.currentTimeMillis(),
            serverReceivedAt = null,
            updatedAt = System.currentTimeMillis()
        )
        quizSessionDao.insertSession(session)

        val _answer = UserAnswerEntity(
            eventId = UUID.randomUUID().toString(),
            supersedesEventId = null,
            mutationType = AnswerMutationType.SELECT,
            sessionId = sessionId,
            userId = userId,
            questionId = UUID.randomUUID().toString(),
            selectedOption = "A",
            isCorrect = null,
            timeTakenSeconds = 10.0,
            attemptSequence = 1,
            idempotencyKey = UUID.randomUUID().toString(),
            clientGeneratedAt = System.currentTimeMillis(),
            serverReceivedAt = null,
            updatedAt = System.currentTimeMillis()
        )

        val syncEvent = SyncEventEntity(
            userId = userId,
            idempotencyKey = _answer.idempotencyKey,
            eventType = SyncEventType.UPSERT_ANSWER,
            payload = "{}",
            status = SyncStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            nextRetryAt = null,
            retryCount = 0,
            lastErrorCode = null,
            lastErrorAt = null
        )

        userAnswerDao.saveAnswerWithOutbox(_answer, syncEvent, System.currentTimeMillis())

        val savedAnswer = userAnswerDao.getLatestAnswerForQuestion(sessionId, _answer.questionId)
        assertNotNull(savedAnswer)
        assertEquals(_answer.eventId, savedAnswer?.eventId)

        val pendingEvents = syncEventDao.getPendingEvents()
        assertEquals(1, pendingEvents.size)

        val updatedSession = quizSessionDao.getSessionById(sessionId)
        assertNotNull(updatedSession)
        assertEquals(2, updatedSession?.sessionVersion)
        assertEquals(_answer.eventId, updatedSession?.lastMutationId)
    }

    @Test
    fun saveAnswerWithOutbox_rollbackOnFailure(): Unit = runBlocking {
        val sessionId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()

        val session = QuizSessionEntity(
            sessionId = sessionId,
            userId = userId,
            quizMetadataId = UUID.randomUUID().toString(),
            status = SessionStatus.IN_PROGRESS,
            startTime = System.currentTimeMillis(),
            lastPausedTime = null,
            endTime = null,
            totalPausedDurationMs = 0,
            activeDurationMs = 0,
            currentQuestionId = null,
            sessionVersion = 1,
            lastMutationId = UUID.randomUUID().toString(),
            idempotencyKey = UUID.randomUUID().toString(),
            deviceFingerprint = null,
            clientGeneratedAt = System.currentTimeMillis(),
            serverReceivedAt = null,
            updatedAt = System.currentTimeMillis()
        )
        quizSessionDao.insertSession(session)

        val _answer = UserAnswerEntity(
            eventId = UUID.randomUUID().toString(),
            supersedesEventId = null,
            mutationType = AnswerMutationType.SELECT,
            sessionId = sessionId,
            userId = userId,
            questionId = UUID.randomUUID().toString(),
            selectedOption = "A",
            isCorrect = null,
            timeTakenSeconds = 10.0,
            attemptSequence = 1,
            idempotencyKey = UUID.randomUUID().toString(),
            clientGeneratedAt = System.currentTimeMillis(),
            serverReceivedAt = null,
            updatedAt = System.currentTimeMillis()
        )

        val syncEvent = SyncEventEntity(
            userId = userId,
            idempotencyKey = _answer.idempotencyKey,
            eventType = SyncEventType.UPSERT_ANSWER,
            payload = "{}",
            status = SyncStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            nextRetryAt = null,
            retryCount = 0,
            lastErrorCode = null,
            lastErrorAt = null
        )

        val implClass = Class.forName("com.cglhustle.core.database.dao.UserAnswerDao_Impl")
        val constructor = implClass.getConstructor(androidx.room.RoomDatabase::class.java)
        val generatedDao = constructor.newInstance(db) as UserAnswerDao

        val failingDao = object : UserAnswerDao() {
            override suspend fun insertAnswerInternal(answer: UserAnswerEntity) {
                implClass.getDeclaredMethod("insertAnswerInternal", UserAnswerEntity::class.java).apply { isAccessible = true }.invoke(generatedDao, answer)
            }

            override suspend fun insertSyncEventInternal(event: SyncEventEntity): Long {
                throw SQLException("Simulated failure after answer persist")
            }

            override suspend fun updateQuizSessionVersionInternal(sessionId: String, lastMutationId: String, updatedAt: Long) {
                implClass.getDeclaredMethod("updateQuizSessionVersionInternal", String::class.java, String::class.java, Long::class.javaPrimitiveType).apply { isAccessible = true }.invoke(generatedDao, sessionId, lastMutationId, updatedAt)
            }

            override suspend fun checkEventExists(userId: String, idempotencyKey: String): Int {
                return 0
            }

            override suspend fun saveAnswerWithOutbox(
                answer: UserAnswerEntity,
                syncEvent: SyncEventEntity,
                timestamp: Long
            ) {
                 db.withTransaction {
                     val exists = checkEventExists(syncEvent.userId, syncEvent.idempotencyKey)
                     insertAnswerInternal(answer)
                     updateQuizSessionVersionInternal(answer.sessionId, answer.eventId, timestamp)
                     if (exists == 0) {
                         insertSyncEventInternal(syncEvent)
                     }
                 }
            }

            override suspend fun getLatestAnswerForQuestion(sessionId: String, questionId: String): UserAnswerEntity? = null
            override suspend fun getAllAnswersForSession(sessionId: String): List<UserAnswerEntity> = emptyList()
        }

        var exceptionThrown = false
        try {
            failingDao.saveAnswerWithOutbox(_answer, syncEvent, System.currentTimeMillis())
        } catch (e: Exception) {
            exceptionThrown = true
        }

        assertEquals(true, exceptionThrown)

        val savedAnswer = userAnswerDao.getLatestAnswerForQuestion(sessionId, _answer.questionId)
        assertNull("Answer should be null because the transaction was rolled back", savedAnswer)

        val sessionAfterFailure = quizSessionDao.getSessionById(sessionId)
        assertEquals(1, sessionAfterFailure?.sessionVersion) // Unchanged
    }
}

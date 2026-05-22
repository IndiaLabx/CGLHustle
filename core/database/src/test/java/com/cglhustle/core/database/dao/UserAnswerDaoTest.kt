package com.cglhustle.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
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
    fun insertAnswer_success(): Unit = runBlocking {
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

        userAnswerDao.insertAnswer(_answer)

        val savedAnswer = userAnswerDao.getLatestAnswerForQuestion(sessionId, _answer.questionId)
        assertNotNull(savedAnswer)
        assertEquals(_answer.eventId, savedAnswer?.eventId)
    }

    @Test
    fun insertSyncEvent_success(): Unit = runBlocking {
        val syncEvent = SyncEventEntity(
            userId = UUID.randomUUID().toString(),
            idempotencyKey = UUID.randomUUID().toString(),
            eventType = SyncEventType.UPSERT_ANSWER,
            payload = "{}",
            status = SyncStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            nextRetryAt = null,
            retryCount = 0,
            lastErrorCode = null,
            lastErrorAt = null
        )

        val id = syncEventDao.insertEvent(syncEvent)
        assert(id > 0)

        val pendingEvents = syncEventDao.getPendingEvents()
        assertEquals(1, pendingEvents.size)
    }
}

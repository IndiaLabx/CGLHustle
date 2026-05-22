package com.cglhustle.core.sync.domain

import com.cglhustle.core.common.error.Success
import com.cglhustle.core.database.dao.QuestionSnapshotDao
import com.cglhustle.core.database.dao.QuizSessionDao
import com.cglhustle.core.database.dao.SyncEventDao
import com.cglhustle.core.database.dao.UserAnswerDao
import com.cglhustle.core.database.entity.AnswerMutationType
import com.cglhustle.core.database.entity.SessionStatus as DbSessionStatus
import com.cglhustle.core.database.entity.SyncEventEntity
import com.cglhustle.core.database.entity.SyncEventType
import com.cglhustle.core.database.entity.SyncStatus
import com.cglhustle.core.database.entity.UserAnswerEntity
import com.cglhustle.core.network.SyncNetworkDataSource
import com.cglhustle.core.network.dto.AnswerMutationRequest
import com.cglhustle.feature.activesession.domain.ActiveSessionData
import com.cglhustle.feature.activesession.domain.ActiveSessionRepository
import com.cglhustle.feature.activesession.domain.Option
import com.cglhustle.feature.activesession.domain.PendingMutation
import com.cglhustle.feature.activesession.domain.Question
import com.cglhustle.feature.activesession.domain.SessionStatus
import com.cglhustle.core.sync.orchestrator.SyncOrchestrator
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ActiveSessionRepositoryImpl @Inject constructor(
    private val questionSnapshotDao: QuestionSnapshotDao,
    private val quizSessionDao: QuizSessionDao,
    private val userAnswerDao: UserAnswerDao,
    private val syncEventDao: SyncEventDao,
    private val syncNetworkDataSource: SyncNetworkDataSource,
    private val syncOrchestrator: SyncOrchestrator
) : ActiveSessionRepository {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    override suspend fun getInitialSessionData(userId: String, sessionId: String): ActiveSessionData? {
        val sessionEntity = quizSessionDao.getSessionById(sessionId) ?: return null
        val answers = userAnswerDao.getAllAnswersForSession(sessionId)

        // In a full implementation, we'd fetch from Server here first.
        // For now, we hydrate from Room as the lightweight cache fallback.

        val selectedAnswers = answers
            .filter { it.mutationType == AnswerMutationType.SELECT && it.selectedOption != null }
            .associate { it.questionId to it.selectedOption!! }

        val status = when (sessionEntity.status) {
            DbSessionStatus.NOT_STARTED, DbSessionStatus.IN_PROGRESS -> SessionStatus.ACTIVE
            DbSessionStatus.PAUSED -> SessionStatus.PAUSED
            DbSessionStatus.SUBMITTED_LOCAL -> SessionStatus.SUBMITTING
            DbSessionStatus.SYNCED_FINAL -> SessionStatus.COMPLETED
            DbSessionStatus.TERMINATED_CONFLICT, DbSessionStatus.ABANDONED -> SessionStatus.COMPLETED
        }

        return ActiveSessionData(
            sessionId = sessionId,
            questions = emptyList(), // Hydrated later in ViewModel
            currentQuestionIndex = 0, // Handled in ViewModel
            selectedAnswers = selectedAnswers,
            pendingMutations = emptyMap(), // Pending mutations should be managed in-memory
            status = status
        )
    }

    override suspend fun getQuestions(sessionId: String): List<Question> {
        val snapshots = questionSnapshotDao.getSnapshotsForSession(sessionId)
        return snapshots.map { snap ->
            val optionsList = snap.options
            Question(
                id = snap.questionId,
                text = snap.questionText,
                options = optionsList.mapIndexed { index, optText ->
                    Option(
                        id = "opt_${index}_${snap.questionId}", // fallback if no real option ID mapping exists
                        text = optText
                    )
                }
            )
        }
    }

    override suspend fun submitAnswer(
        userId: String,
        sessionId: String,
        questionId: String,
        eventId: String,
        idempotencyKey: String,
        selectedOptionId: String,
        attemptSequence: Int
    ): Result<Unit> {
        val timestamp = System.currentTimeMillis()

        val request = AnswerMutationRequest(
            userId = userId,
            sessionId = sessionId,
            questionId = questionId,
            eventId = eventId,
            attemptSequence = attemptSequence,
            idempotencyKey = idempotencyKey
        )

        // 1. Direct Network Call FIRST
        val networkResult = syncNetworkDataSource.submitAnswer(request)

        // 2. Room is just a lightweight cache
        val answer = UserAnswerEntity(
            eventId = eventId,
            supersedesEventId = null,
            mutationType = AnswerMutationType.SELECT,
            sessionId = sessionId,
            userId = userId,
            questionId = questionId,
            selectedOption = selectedOptionId,
            isCorrect = null,
            timeTakenSeconds = null,
            attemptSequence = attemptSequence,
            idempotencyKey = idempotencyKey,
            clientGeneratedAt = timestamp,
            serverReceivedAt = if (networkResult is Success) timestamp else null,
            updatedAt = timestamp
        )

        // We use a simple insert, removing the forced outbox transaction logic
        userAnswerDao.insertAnswer(answer)
        quizSessionDao.updateSessionVersion(sessionId, eventId, timestamp)

        // 3. Fallback Retry Mechanism
        if (networkResult !is Success) {
            val syncEvent = SyncEventEntity(
                userId = userId,
                idempotencyKey = idempotencyKey,
                eventType = SyncEventType.UPSERT_ANSWER,
                payload = json.encodeToString(request),
                status = SyncStatus.PENDING,
                createdAt = timestamp,
                nextRetryAt = null,
                retryCount = 0,
                lastErrorCode = null,
                lastErrorAt = null
            )
            val exists = syncEventDao.checkEventExists(userId, idempotencyKey)
            if (exists == 0) {
                syncEventDao.insertEvent(syncEvent)
                syncOrchestrator.enqueueSync()
            }
        }

        return Result.success(Unit)
    }

    override suspend fun pauseSession(sessionId: String): Result<Boolean> {
        val timestamp = System.currentTimeMillis()
        quizSessionDao.updateSessionPauseState(
            sessionId = sessionId,
            status = DbSessionStatus.PAUSED.name,
            lastMutationId = "pause_${timestamp}",
            updatedAt = timestamp,
            totalPausedDurationMs = 0L, // Need to accumulate in real scenario
            lastPausedTime = timestamp
        )
        return Result.success(true)
    }

    override suspend fun resumeSession(sessionId: String): Result<Boolean> {
        val timestamp = System.currentTimeMillis()
        quizSessionDao.updateSessionPauseState(
            sessionId = sessionId,
            status = DbSessionStatus.IN_PROGRESS.name,
            lastMutationId = "resume_${timestamp}",
            updatedAt = timestamp,
            totalPausedDurationMs = 0L, // Accumulate real scenario
            lastPausedTime = null
        )
        return Result.success(true)
    }

    override suspend fun submitSession(sessionId: String): Result<Boolean> {
        val timestamp = System.currentTimeMillis()
        quizSessionDao.updateSessionStatus(
            sessionId = sessionId,
            status = DbSessionStatus.SUBMITTED_LOCAL.name,
            lastMutationId = "submit_${timestamp}",
            updatedAt = timestamp
        )

        val session = quizSessionDao.getSessionById(sessionId)
        if (session != null) {
            val event = SyncEventEntity(
                userId = session.userId,
                idempotencyKey = "submit_${sessionId}_${timestamp}",
                eventType = SyncEventType.MARK_COMPLETED,
                payload = "{\"session_id\":\"$sessionId\"}", // minimal payload
                status = SyncStatus.PENDING,
                createdAt = timestamp,
                nextRetryAt = null,
                retryCount = 0,
                lastErrorCode = null,
                lastErrorAt = null
            )
            syncEventDao.insertEvent(event)
            syncOrchestrator.enqueueSync()
        }

        return Result.success(true)
    }
}

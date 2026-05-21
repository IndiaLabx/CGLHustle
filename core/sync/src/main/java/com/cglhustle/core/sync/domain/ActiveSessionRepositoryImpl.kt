package com.cglhustle.core.sync.domain

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
import com.cglhustle.core.network.dto.AnswerMutationRequest
import com.cglhustle.feature.activesession.domain.ActiveSessionData
import com.cglhustle.feature.activesession.domain.ActiveSessionRepository
import com.cglhustle.feature.activesession.domain.Option
import com.cglhustle.feature.activesession.domain.PendingMutation
import com.cglhustle.feature.activesession.domain.Question
import com.cglhustle.feature.activesession.domain.SessionStatus
import com.cglhustle.core.sync.orchestrator.SyncOrchestrator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ActiveSessionRepositoryImpl @Inject constructor(
    private val questionSnapshotDao: QuestionSnapshotDao,
    private val quizSessionDao: QuizSessionDao,
    private val userAnswerDao: UserAnswerDao,
    private val syncEventDao: SyncEventDao,
    private val syncOrchestrator: SyncOrchestrator
) : ActiveSessionRepository {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    override fun observeSessionData(userId: String, sessionId: String): Flow<ActiveSessionData?> {
        val sessionFlow = quizSessionDao.observeSessionById(sessionId).filterNotNull()
        val answersFlow = userAnswerDao.observeAllAnswersForSession(sessionId)
        val syncEventsFlow = syncEventDao.observeAllSyncEvents(userId)

        return combine(sessionFlow, answersFlow, syncEventsFlow) { sessionEntity, answers, syncEvents ->

            // Map Answers
            val selectedAnswers = answers
                .filter { it.mutationType == AnswerMutationType.SELECT && it.selectedOption != null }
                .associate { it.questionId to it.selectedOption!! }

            // Pending Mutations
            val pendingMutations = mutableMapOf<String, PendingMutation>()

            val pendingSyncEvents = syncEvents.filter {
                it.status == SyncStatus.PENDING || it.status == SyncStatus.IN_FLIGHT || it.status == SyncStatus.FAILED_RETRY
            }

            for (event in pendingSyncEvents) {
                if (event.eventType == SyncEventType.UPSERT_ANSWER) {
                    try {
                        val payload = json.decodeFromString<AnswerMutationRequest>(event.payload)
                        if (payload.sessionId == sessionId) {
                            val matchingAnswer = answers.find { it.eventId == payload.eventId }
                            if (matchingAnswer != null && matchingAnswer.selectedOption != null) {
                                pendingMutations[payload.questionId] = PendingMutation(
                                    questionId = payload.questionId,
                                    selectedOptionId = matchingAnswer.selectedOption!!,
                                    eventId = payload.eventId
                                )
                            }
                        }
                    } catch (e: Exception) {
                        // Ignore parse errors on old schema events
                    }
                }
            }

            // Status mapping
            val status = when (sessionEntity.status) {
                DbSessionStatus.NOT_STARTED, DbSessionStatus.IN_PROGRESS -> SessionStatus.ACTIVE
                DbSessionStatus.PAUSED -> SessionStatus.PAUSED
                DbSessionStatus.SUBMITTED_LOCAL -> SessionStatus.SUBMITTING
                DbSessionStatus.SYNCED_FINAL -> SessionStatus.COMPLETED
                DbSessionStatus.TERMINATED_CONFLICT, DbSessionStatus.ABANDONED -> SessionStatus.COMPLETED
            }

            ActiveSessionData(
                sessionId = sessionId,
                questions = emptyList(), // Hydrated later in ViewModel to avoid redundant fetching
                currentQuestionIndex = 0, // Handled in ViewModel
                selectedAnswers = selectedAnswers,
                pendingMutations = pendingMutations,
                status = status
            )
        }
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

        val answer = UserAnswerEntity(
            eventId = eventId,
            supersedesEventId = null,
            mutationType = AnswerMutationType.SELECT,
            sessionId = sessionId,
            userId = userId,
            questionId = questionId,
            selectedOption = selectedOptionId,
            isCorrect = null, // Backend evaluated
            timeTakenSeconds = null,
            attemptSequence = attemptSequence,
            idempotencyKey = idempotencyKey,
            clientGeneratedAt = timestamp,
            serverReceivedAt = null,
            updatedAt = timestamp
        )

        val request = AnswerMutationRequest(
            userId = userId,
            sessionId = sessionId,
            questionId = questionId,
            eventId = eventId,
            attemptSequence = attemptSequence,
            idempotencyKey = idempotencyKey
        )

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

        userAnswerDao.saveAnswerWithOutbox(answer, syncEvent, timestamp)
        syncOrchestrator.enqueueSync()
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

        // Ensure a SYNC EVENT is added to submit session to server.
        // We will mock idempotency key and user ID for now since they aren't provided here, but they should be in the DB.
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
            // Just insert into sync event directly because it's local submission
            syncEventDao.insertEvent(event)
        }

        syncOrchestrator.enqueueSync()
        return Result.success(true)
    }
}

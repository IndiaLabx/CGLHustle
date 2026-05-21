package com.cglhustle.core.sync.domain

import com.cglhustle.core.network.CglHustleApi
import com.cglhustle.core.network.dto.QuizConfigPayloadDto
import com.cglhustle.core.database.dao.QuestionSnapshotDao
import com.cglhustle.core.database.dao.QuizSessionDao
import com.cglhustle.core.database.entity.QuestionSnapshotEntity
import com.cglhustle.core.database.entity.QuizSessionEntity
import com.cglhustle.core.database.entity.SessionStatus as DbSessionStatus
import com.cglhustle.feature.quizconfig.domain.model.QuizConfigPayload
import com.cglhustle.feature.quizconfig.domain.model.QuizFilterOptions
import com.cglhustle.feature.quizconfig.domain.repository.QuizConfigRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class QuizConfigRepositoryImpl @Inject constructor(
    private val api: CglHustleApi,
    private val questionSnapshotDao: QuestionSnapshotDao,
    private val quizSessionDao: QuizSessionDao
) : QuizConfigRepository {

    override suspend fun fetchAvailableFilters(): QuizFilterOptions {
        val dto = api.getQuizFilters()
        return QuizFilterOptions(
            subjects = dto.subjects,
            topics = dto.topics,
            subTopics = dto.subTopics,
            difficulties = dto.difficulties,
            examYears = dto.examYears,
            shifts = dto.shifts
        )
    }

    override suspend fun createSession(payload: QuizConfigPayload): String {
        val dto = QuizConfigPayloadDto(
            subject = payload.subject,
            topic = payload.topic,
            subTopic = payload.subTopic,
            difficulty = payload.difficulty,
            examYear = payload.examYear,
            shift = payload.shift,
            mode = payload.mode.name,
            questionCount = payload.questionCount,
            questionType = payload.questionType,
            quizName = payload.quizName
        )

        val response = api.createQuizSession(dto)
        val sessionId = response.session.sessionId

        // Hydrate questions
        val json = Json { encodeDefaults = true }
        val snapshotEntities = response.questions.map { q ->
            QuestionSnapshotEntity(
                id = q.id,
                userId = q.userId,
                quizSessionId = q.quizSessionId,
                questionId = q.questionId,
                contentVersion = q.contentVersion,
                snapshotHash = q.snapshotHash,
                sourceProject = q.sourceProject,
                sourceFetchedAt = q.sourceFetchedAt,
                languagePackVersion = q.languagePackVersion,
                isDeletedUpstream = q.isDeletedUpstream,
                subject = q.subject,
                topic = q.topic,
                difficulty = q.difficulty,
                questionType = q.questionType,
                questionText = q.questionText,
                questionTextHi = q.questionTextHi,
                options = q.options,
                optionsHi = q.optionsHi,
                correctAnswer = q.correctAnswer,
                explanation = q.explanation,
                tags = q.tags
            )
        }
        questionSnapshotDao.insertSnapshots(snapshotEntities)

        // Hydrate Session
        val sessionEntity = QuizSessionEntity(
            sessionId = response.session.sessionId,
            userId = response.session.userId,
            quizMetadataId = response.session.quizMetadataId,
            status = DbSessionStatus.NOT_STARTED,
            startTime = null,
            lastPausedTime = null,
            endTime = null,
            totalPausedDurationMs = 0L,
            activeDurationMs = 0L,
            currentQuestionId = null,
            sessionVersion = response.session.sessionVersion,
            lastMutationId = response.session.lastMutationId,
            idempotencyKey = "", // Would come from response if needed
            deviceFingerprint = null,
            clientGeneratedAt = System.currentTimeMillis(),
            serverReceivedAt = null,
            updatedAt = System.currentTimeMillis()
        )
        quizSessionDao.insertSession(sessionEntity)

        return sessionId
    }
}

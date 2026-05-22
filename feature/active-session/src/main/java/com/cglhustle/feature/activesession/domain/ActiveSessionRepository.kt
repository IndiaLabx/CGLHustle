package com.cglhustle.feature.activesession.domain

interface ActiveSessionRepository {
    suspend fun getInitialSessionData(userId: String, sessionId: String): ActiveSessionData?

    suspend fun getQuestions(sessionId: String): List<Question>
    suspend fun submitAnswer(
        userId: String,
        sessionId: String,
        questionId: String,
        eventId: String,
        idempotencyKey: String,
        selectedOptionId: String,
        attemptSequence: Int
    ): Result<Unit>

    suspend fun pauseSession(sessionId: String): Result<Boolean>
    suspend fun resumeSession(sessionId: String): Result<Boolean>
    suspend fun submitSession(sessionId: String): Result<Boolean>
}

data class Question(
    val id: String,
    val text: String,
    val options: List<Option>
)

data class Option(
    val id: String,
    val text: String
)

enum class FeatureMutationStatus {
    APPLIED,
    NOOP,
    CONFLICT
}

data class FeatureMutationAckResponse(
    val status: FeatureMutationStatus,
    val canonicalSequence: Long
)

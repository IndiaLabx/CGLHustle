package com.cglhustle.feature.activesession.domain

data class Option(
    val id: String,
    val text: String
)

data class Question(
    val id: String,
    val text: String,
    val options: List<Option>
)

enum class SessionStatus {
    ACTIVE,
    PAUSED,
    SUBMITTING,
    COMPLETED
}

data class PendingMutation(
    val questionId: String,
    val selectedOptionId: String,
    val eventId: String
)

data class ActiveSessionData(
    val sessionId: String,
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<String, String> = emptyMap(), // questionId -> optionId
    val pendingMutations: Map<String, PendingMutation> = emptyMap(), // questionId -> PendingMutation
    val status: SessionStatus = SessionStatus.ACTIVE
) {
    val currentQuestion: Question?
        get() = questions.getOrNull(currentQuestionIndex)
}

package com.cglhustle.feature.activesession.domain

import com.cglhustle.core.network.dto.AnswerMutationRequest
import com.cglhustle.core.network.dto.MutationAckResponse

interface ActiveSessionRepository {
    suspend fun getQuestions(sessionId: String): List<Question>
    suspend fun submitAnswer(request: AnswerMutationRequest): MutationAckResponse
    suspend fun pauseSession(sessionId: String): Boolean
    suspend fun resumeSession(sessionId: String): Boolean
    suspend fun submitSession(sessionId: String): Boolean
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

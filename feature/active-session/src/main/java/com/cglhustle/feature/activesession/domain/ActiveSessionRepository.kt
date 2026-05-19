package com.cglhustle.feature.activesession.domain

import com.cglhustle.core.network.dto.MutationAckResponse

interface ActiveSessionRepository {
    suspend fun getQuestions(sessionId: String): List<Question>
    suspend fun submitAnswer(
        sessionId: String,
        questionId: String,
        optionId: String,
        eventId: String
    ): Result<MutationAckResponse>

    suspend fun submitSession(sessionId: String): Result<Unit>
}

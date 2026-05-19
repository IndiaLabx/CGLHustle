package com.cglhustle.feature.activesession.data

import com.cglhustle.core.network.dto.MutationAckResponse
import com.cglhustle.core.network.dto.MutationStatus
import com.cglhustle.feature.activesession.domain.ActiveSessionRepository
import com.cglhustle.feature.activesession.domain.Option
import com.cglhustle.feature.activesession.domain.Question
import kotlinx.coroutines.delay
import javax.inject.Inject

class ActiveSessionRepositoryImpl @Inject constructor() : ActiveSessionRepository {

    override suspend fun getQuestions(sessionId: String): List<Question> {
        delay(1000) // Mock latency
        return listOf(
            Question(
                id = "q1",
                text = "What is the capital of France?",
                options = listOf(
                    Option("o1", "London"),
                    Option("o2", "Paris"),
                    Option("o3", "Berlin"),
                    Option("o4", "Madrid")
                )
            ),
            Question(
                id = "q2",
                text = "Which planet is known as the Red Planet?",
                options = listOf(
                    Option("o5", "Earth"),
                    Option("o6", "Jupiter"),
                    Option("o7", "Mars"),
                    Option("o8", "Venus")
                )
            ),
            Question(
                id = "q3",
                text = "What is the largest ocean on Earth?",
                options = listOf(
                    Option("o9", "Atlantic"),
                    Option("o10", "Indian"),
                    Option("o11", "Arctic"),
                    Option("o12", "Pacific")
                )
            )
        )
    }

    override suspend fun submitAnswer(
        sessionId: String,
        questionId: String,
        optionId: String,
        eventId: String
    ): Result<MutationAckResponse> {
        delay(800) // Mock latency

        // Mock a 10% chance of conflict for testing reconciliation
        val isConflict = Math.random() < 0.1
        val status = if (isConflict) MutationStatus.CONFLICT else MutationStatus.APPLIED

        return Result.success(
            MutationAckResponse(
                status = status,
                canonicalSequence = System.currentTimeMillis()
            )
        )
    }

    override suspend fun submitSession(sessionId: String): Result<Unit> {
        delay(1500) // Mock latency for final submission
        return Result.success(Unit)
    }
}

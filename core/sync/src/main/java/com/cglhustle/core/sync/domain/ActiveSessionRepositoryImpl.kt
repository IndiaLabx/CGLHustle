package com.cglhustle.core.sync.domain

import com.cglhustle.core.network.dto.AnswerMutationRequest
import com.cglhustle.core.network.dto.MutationAckResponse
import com.cglhustle.core.network.dto.MutationStatus
import com.cglhustle.feature.activesession.domain.ActiveSessionRepository
import com.cglhustle.feature.activesession.domain.Option
import com.cglhustle.feature.activesession.domain.Question
import com.cglhustle.feature.activesession.domain.FeatureMutationAckResponse
import com.cglhustle.feature.activesession.domain.FeatureMutationStatus
import kotlinx.coroutines.delay
import javax.inject.Inject

class ActiveSessionRepositoryImpl @Inject constructor() : ActiveSessionRepository {

    override suspend fun getQuestions(sessionId: String): List<Question> {
        delay(500) // Simulate network/DB latency
        return listOf(
            Question(
                id = "q1",
                text = "What is the capital of France?",
                options = listOf(
                    Option("o1", "Berlin"),
                    Option("o2", "Madrid"),
                    Option("o3", "Paris"),
                    Option("o4", "Rome")
                )
            ),
            Question(
                id = "q2",
                text = "Which planet is known as the Red Planet?",
                options = listOf(
                    Option("o5", "Earth"),
                    Option("o6", "Mars"),
                    Option("o7", "Jupiter"),
                    Option("o8", "Saturn")
                )
            )
        )
    }

    override suspend fun submitAnswer(
        userId: String,
        sessionId: String,
        questionId: String,
        eventId: String,
        idempotencyKey: String
    ): Result<FeatureMutationAckResponse> {
        return try {
            delay(800) // Simulate network latency
            val isConflict = Math.random() > 0.9
            if (isConflict) {
                Result.success(FeatureMutationAckResponse(status = FeatureMutationStatus.CONFLICT, canonicalSequence = System.currentTimeMillis()))
            } else {
                Result.success(FeatureMutationAckResponse(status = FeatureMutationStatus.APPLIED, canonicalSequence = System.currentTimeMillis()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pauseSession(sessionId: String): Result<Boolean> {
        return try {
            delay(300)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resumeSession(sessionId: String): Result<Boolean> {
        return try {
            delay(300)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitSession(sessionId: String): Result<Boolean> {
        return try {
            delay(1500)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

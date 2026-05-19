package com.cglhustle.feature.results.data.repository

import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.AppResult
import com.cglhustle.core.common.error.Success
import com.cglhustle.feature.results.data.remote.dto.AttemptedQuestionDto
import com.cglhustle.feature.results.data.remote.dto.BookmarkedQuestionDto
import com.cglhustle.feature.results.data.remote.dto.ResultsAnalyticsDto
import com.cglhustle.feature.results.domain.repository.ResultsRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class ResultsRepositoryImpl @Inject constructor() : ResultsRepository {

    override suspend fun getAnalytics(sessionId: String): AppResult<ResultsAnalyticsDto, AppError> {
        delay(800)
        return Success(
            ResultsAnalyticsDto(
                sessionId = sessionId,
                finalScore = 8,
                totalQuestions = 10,
                rank = 42,
                timeTakenSeconds = 120
            )
        )
    }

    override suspend fun getAttemptedQuestions(sessionId: String): AppResult<List<AttemptedQuestionDto>, AppError> {
        delay(800)
        return Success(
            listOf(
                AttemptedQuestionDto(
                    questionId = "q1",
                    questionText = "What is the capital of France?",
                    isCorrect = true,
                    userAnswer = "Paris",
                    correctAnswer = "Paris"
                ),
                AttemptedQuestionDto(
                    questionId = "q2",
                    questionText = "What is 2 + 2?",
                    isCorrect = false,
                    userAnswer = "5",
                    correctAnswer = "4"
                )
            )
        )
    }

    override suspend fun getBookmarkedQuestions(sessionId: String): AppResult<List<BookmarkedQuestionDto>, AppError> {
        delay(800)
        return Success(
            listOf(
                BookmarkedQuestionDto(
                    questionId = "q3",
                    questionText = "What is the speed of light?",
                    bookmarkedAt = System.currentTimeMillis()
                )
            )
        )
    }
}

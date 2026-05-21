package com.cglhustle.core.sync.domain

import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.AppResult
import com.cglhustle.core.common.error.Success
import com.cglhustle.core.common.error.Failure
import com.cglhustle.core.network.CglHustleApi
import com.cglhustle.feature.results.domain.model.AttemptedQuestion
import com.cglhustle.feature.results.domain.model.BookmarkedQuestion
import com.cglhustle.feature.results.domain.model.ResultsAnalytics
import com.cglhustle.feature.results.domain.repository.ResultsRepository
import javax.inject.Inject

class ResultsRepositoryImpl @Inject constructor(
    private val api: CglHustleApi
) : ResultsRepository {

    override suspend fun getAnalytics(sessionId: String): AppResult<ResultsAnalytics, AppError> {
        return try {
            val dto = api.getQuizResult(sessionId)
            val model = ResultsAnalytics(
                sessionId = dto.sessionId,
                finalScore = dto.finalScore,
                totalQuestions = dto.totalQuestions,
                rank = dto.rank,
                timeTakenSeconds = dto.timeTakenSeconds
            )
            Success(model)
        } catch (e: Exception) {
            Failure(com.cglhustle.core.common.error.UnknownError(e))
        }
    }

    override suspend fun getAttemptedQuestions(sessionId: String): AppResult<List<AttemptedQuestion>, AppError> {
        return try {
            val dtoList = api.getAttemptedQuestions(sessionId)
            val modelList = dtoList.map { dto ->
                AttemptedQuestion(
                    questionId = dto.questionId,
                    questionText = dto.questionText,
                    isCorrect = dto.isCorrect,
                    userAnswer = dto.userAnswer,
                    correctAnswer = dto.correctAnswer
                )
            }
            Success(modelList)
        } catch (e: Exception) {
            Failure(com.cglhustle.core.common.error.UnknownError(e))
        }
    }

    override suspend fun getBookmarkedQuestions(sessionId: String): AppResult<List<BookmarkedQuestion>, AppError> {
        return try {
            val dtoList = api.getBookmarks()
            val modelList = dtoList.map { dto ->
                BookmarkedQuestion(
                    questionId = dto.questionId,
                    questionText = dto.questionText,
                    bookmarkedAt = dto.bookmarkedAt
                )
            }
            Success(modelList)
        } catch (e: Exception) {
            Failure(com.cglhustle.core.common.error.UnknownError(e))
        }
    }
}

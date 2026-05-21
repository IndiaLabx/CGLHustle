package com.cglhustle.feature.results.data.repository

import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.AppResult
import com.cglhustle.core.common.error.Success
import com.cglhustle.core.common.error.Failure
import com.cglhustle.core.network.dto.AttemptedQuestionDto
import com.cglhustle.core.network.dto.BookmarkedQuestionDto
import com.cglhustle.core.network.dto.ResultsAnalyticsDto
import com.cglhustle.core.network.CglHustleApi
import com.cglhustle.feature.results.domain.repository.ResultsRepository
import javax.inject.Inject

class ResultsRepositoryImpl @Inject constructor(
    private val api: CglHustleApi
) : ResultsRepository {

    override suspend fun getAnalytics(sessionId: String): AppResult<ResultsAnalyticsDto, AppError> {
        return try {
            Success(api.getQuizResult(sessionId))
        } catch (e: Exception) {
            Failure(com.cglhustle.core.common.error.UnknownError(e))
        }
    }

    override suspend fun getAttemptedQuestions(sessionId: String): AppResult<List<AttemptedQuestionDto>, AppError> {
        return try {
            Success(api.getAttemptedQuestions(sessionId))
        } catch (e: Exception) {
            Failure(com.cglhustle.core.common.error.UnknownError(e))
        }
    }

    override suspend fun getBookmarkedQuestions(sessionId: String): AppResult<List<BookmarkedQuestionDto>, AppError> {
        return try {
            Success(api.getBookmarks())
        } catch (e: Exception) {
            Failure(com.cglhustle.core.common.error.UnknownError(e))
        }
    }
}

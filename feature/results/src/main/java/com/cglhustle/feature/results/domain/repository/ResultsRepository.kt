package com.cglhustle.feature.results.domain.repository

import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.AppResult
import com.cglhustle.feature.results.data.remote.dto.AttemptedQuestionDto
import com.cglhustle.feature.results.data.remote.dto.BookmarkedQuestionDto
import com.cglhustle.feature.results.data.remote.dto.ResultsAnalyticsDto

interface ResultsRepository {
    suspend fun getAnalytics(sessionId: String): AppResult<ResultsAnalyticsDto, AppError>
    suspend fun getAttemptedQuestions(sessionId: String): AppResult<List<AttemptedQuestionDto>, AppError>
    suspend fun getBookmarkedQuestions(sessionId: String): AppResult<List<BookmarkedQuestionDto>, AppError>
}

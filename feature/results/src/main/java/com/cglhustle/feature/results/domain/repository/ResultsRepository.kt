package com.cglhustle.feature.results.domain.repository

import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.AppResult
import com.cglhustle.core.network.dto.AttemptedQuestionDto
import com.cglhustle.core.network.dto.BookmarkedQuestionDto
import com.cglhustle.core.network.dto.ResultsAnalyticsDto

interface ResultsRepository {
    suspend fun getAnalytics(sessionId: String): AppResult<ResultsAnalyticsDto, AppError>
    suspend fun getAttemptedQuestions(sessionId: String): AppResult<List<AttemptedQuestionDto>, AppError>
    suspend fun getBookmarkedQuestions(sessionId: String): AppResult<List<BookmarkedQuestionDto>, AppError>
}

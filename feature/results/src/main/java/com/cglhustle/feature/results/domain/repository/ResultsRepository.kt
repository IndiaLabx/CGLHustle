package com.cglhustle.feature.results.domain.repository

import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.AppResult
import com.cglhustle.feature.results.domain.model.AttemptedQuestion
import com.cglhustle.feature.results.domain.model.BookmarkedQuestion
import com.cglhustle.feature.results.domain.model.ResultsAnalytics

interface ResultsRepository {
    suspend fun getAnalytics(sessionId: String): AppResult<ResultsAnalytics, AppError>
    suspend fun getAttemptedQuestions(sessionId: String): AppResult<List<AttemptedQuestion>, AppError>
    suspend fun getBookmarkedQuestions(sessionId: String): AppResult<List<BookmarkedQuestion>, AppError>
}

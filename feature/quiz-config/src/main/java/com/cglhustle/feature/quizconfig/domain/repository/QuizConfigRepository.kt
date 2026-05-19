package com.cglhustle.feature.quizconfig.domain.repository

import com.cglhustle.feature.quizconfig.domain.model.QuizConfigPayload
import com.cglhustle.feature.quizconfig.domain.model.QuizFilterOptions

interface QuizConfigRepository {
    suspend fun fetchAvailableFilters(): QuizFilterOptions
    suspend fun createSession(payload: QuizConfigPayload): String
}

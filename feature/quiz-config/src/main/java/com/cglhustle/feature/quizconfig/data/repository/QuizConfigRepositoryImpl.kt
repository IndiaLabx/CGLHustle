package com.cglhustle.feature.quizconfig.data.repository

import com.cglhustle.feature.quizconfig.data.remote.dto.QuizFiltersDto
import com.cglhustle.feature.quizconfig.data.remote.dto.toDomainModel
import com.cglhustle.feature.quizconfig.domain.model.QuizConfigPayload
import com.cglhustle.feature.quizconfig.domain.model.QuizFilterOptions
import com.cglhustle.feature.quizconfig.domain.repository.QuizConfigRepository
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject

class QuizConfigRepositoryImpl @Inject constructor() : QuizConfigRepository {

    override suspend fun fetchAvailableFilters(): QuizFilterOptions {
        delay(1000) // Simulate network delay

        val mockDto = QuizFiltersDto(
            subjects = listOf("Math", "Physics", "Chemistry"),
            topics = listOf("Algebra", "Mechanics", "Organic"),
            difficulties = listOf("Easy", "Medium", "Hard"),
            examYears = listOf("2023", "2024"),
            shifts = listOf("Morning", "Evening")
        )

        return mockDto.toDomainModel()
    }

    override suspend fun createSession(payload: QuizConfigPayload): String {
        delay(1000) // Simulate network delay
        return UUID.randomUUID().toString()
    }
}

package com.cglhustle.feature.quizconfig.data.remote.dto

data class QuizFiltersDto(
    val subjects: List<String>,
    val topics: List<String>,
    val difficulties: List<String>,
    val examYears: List<String>,
    val shifts: List<String>
)

fun QuizFiltersDto.toDomainModel() = com.cglhustle.feature.quizconfig.domain.model.QuizFilterOptions(
    subjects = subjects,
    topics = topics,
    difficulties = difficulties,
    examYears = examYears,
    shifts = shifts
)

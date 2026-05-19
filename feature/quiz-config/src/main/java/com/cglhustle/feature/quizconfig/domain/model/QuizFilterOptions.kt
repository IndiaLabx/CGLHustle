package com.cglhustle.feature.quizconfig.domain.model

data class QuizFilterOptions(
    val subjects: List<String>,
    val topics: List<String>,
    val difficulties: List<String>,
    val examYears: List<String>,
    val shifts: List<String>
)

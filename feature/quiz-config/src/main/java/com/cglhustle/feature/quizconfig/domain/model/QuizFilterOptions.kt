package com.cglhustle.feature.quizconfig.domain.model

data class QuizFilterOptions(
    val subjects: List<String> = emptyList(),
    val topics: List<String> = emptyList(),
    val subTopics: List<String> = emptyList(),
    val difficulties: List<String> = emptyList(),
    val examNames: List<String> = emptyList(),
    val examYears: List<String> = emptyList(),
    val shifts: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)

package com.cglhustle.feature.quizconfig.domain.model

data class QuizConfigPayload(
    val subjects: List<String> = emptyList(),
    val topics: List<String> = emptyList(),
    val subTopics: List<String> = emptyList(),
    val difficulty: String,
    val examNames: List<String> = emptyList(),
    val examYears: List<String> = emptyList(),
    val shifts: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val mode: QuizMode = QuizMode.LEARNING,
    val questionCount: Int = 10,
    val questionType: String = "MCQ",
    val quizName: String? = null
)

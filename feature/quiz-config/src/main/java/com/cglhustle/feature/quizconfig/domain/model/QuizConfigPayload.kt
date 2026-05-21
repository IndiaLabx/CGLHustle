package com.cglhustle.feature.quizconfig.domain.model

data class QuizConfigPayload(
    val subject: String,
    val topic: String,
    val subTopic: String? = null,
    val difficulty: String,
    val examYear: String,
    val shift: String,
    val mode: QuizMode = QuizMode.LEARNING,
    val questionCount: Int = 10,
    val questionType: String = "MCQ",
    val quizName: String? = null
)

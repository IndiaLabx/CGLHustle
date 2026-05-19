package com.cglhustle.feature.quizconfig.domain.model

data class QuizConfigPayload(
    val subject: String,
    val topic: String,
    val difficulty: String,
    val examYear: String,
    val shift: String
)

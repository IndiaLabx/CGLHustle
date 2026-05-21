package com.cglhustle.feature.results.domain.model

data class AttemptedQuestion(
    val questionId: String,
    val questionText: String,
    val isCorrect: Boolean,
    val userAnswer: String,
    val correctAnswer: String
)

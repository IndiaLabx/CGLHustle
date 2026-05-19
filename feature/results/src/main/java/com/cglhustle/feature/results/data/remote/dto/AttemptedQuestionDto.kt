package com.cglhustle.feature.results.data.remote.dto

data class AttemptedQuestionDto(
    val questionId: String,
    val questionText: String,
    val isCorrect: Boolean,
    val userAnswer: String,
    val correctAnswer: String
)

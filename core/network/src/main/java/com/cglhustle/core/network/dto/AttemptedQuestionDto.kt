package com.cglhustle.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttemptedQuestionDto(
    @SerialName("question_id") val questionId: String,
    @SerialName("question_text") val questionText: String,
    @SerialName("is_correct") val isCorrect: Boolean,
    @SerialName("user_answer") val userAnswer: String,
    @SerialName("correct_answer") val correctAnswer: String
)

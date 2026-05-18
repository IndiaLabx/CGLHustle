package com.cglhustle.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserAnswerDto(
    @SerialName("id")
    val id: String,
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("question_id")
    val questionId: String,
    @SerialName("selected_option_id")
    val selectedOptionId: String?,
    @SerialName("is_correct")
    val isCorrect: Boolean,
    @SerialName("metadata")
    val metadata: String, // serialized map
    @SerialName("created_at")
    val createdAt: Long
)

package com.cglhustle.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class QuizSessionResponse(
    val sessionId: String,
    val userId: String,
    val sessionStatus: String,
    val serverUpdatedAt: Long
)

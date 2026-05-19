package com.cglhustle.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class AnswerMutationRequest(
    val userId: String,
    val sessionId: String,
    val questionId: String,
    val eventId: String,
    val idempotencyKey: String
)

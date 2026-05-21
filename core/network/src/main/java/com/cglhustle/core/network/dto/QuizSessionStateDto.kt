package com.cglhustle.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizSessionStateDto(
    @SerialName("session_id") val sessionId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("quiz_metadata_id") val quizMetadataId: String,
    @SerialName("status") val status: String,
    @SerialName("session_version") val sessionVersion: Int,
    @SerialName("last_mutation_id") val lastMutationId: String,
    @SerialName("start_time") val startTime: String?,
    @SerialName("last_paused_time") val lastPausedTime: String?,
    @SerialName("end_time") val endTime: String?,
    @SerialName("total_paused_duration_ms") val totalPausedDurationMs: Long,
    @SerialName("active_duration_ms") val activeDurationMs: Long,
    @SerialName("client_generated_at") val clientGeneratedAt: String,
    @SerialName("server_received_at") val serverReceivedAt: String?,
    @SerialName("updated_at") val updatedAt: String
)

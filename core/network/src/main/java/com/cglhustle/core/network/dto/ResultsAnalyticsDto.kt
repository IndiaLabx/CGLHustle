package com.cglhustle.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResultsAnalyticsDto(
    @SerialName("session_id") val sessionId: String,
    @SerialName("final_score") val finalScore: Int,
    @SerialName("total_questions") val totalQuestions: Int,
    @SerialName("rank") val rank: Int,
    @SerialName("time_taken_seconds") val timeTakenSeconds: Int
)

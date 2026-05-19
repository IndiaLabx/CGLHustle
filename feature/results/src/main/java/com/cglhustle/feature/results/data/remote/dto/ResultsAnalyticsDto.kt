package com.cglhustle.feature.results.data.remote.dto

data class ResultsAnalyticsDto(
    val sessionId: String,
    val finalScore: Int,
    val totalQuestions: Int,
    val rank: Int,
    val timeTakenSeconds: Int
)

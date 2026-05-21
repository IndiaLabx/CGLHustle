package com.cglhustle.feature.results.domain.model

data class ResultsAnalytics(
    val sessionId: String,
    val finalScore: Int,
    val totalQuestions: Int,
    val rank: Int,
    val timeTakenSeconds: Int
)

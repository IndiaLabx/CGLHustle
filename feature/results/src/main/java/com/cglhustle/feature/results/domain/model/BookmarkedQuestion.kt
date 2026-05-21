package com.cglhustle.feature.results.domain.model

data class BookmarkedQuestion(
    val questionId: String,
    val questionText: String,
    val bookmarkedAt: Long
)

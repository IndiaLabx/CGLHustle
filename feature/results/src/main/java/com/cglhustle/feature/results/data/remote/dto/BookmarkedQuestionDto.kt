package com.cglhustle.feature.results.data.remote.dto

data class BookmarkedQuestionDto(
    val questionId: String,
    val questionText: String,
    val bookmarkedAt: Long
)

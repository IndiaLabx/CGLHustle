package com.cglhustle.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookmarkedQuestionDto(
    @SerialName("question_id") val questionId: String,
    @SerialName("question_text") val questionText: String,
    @SerialName("bookmarked_at") val bookmarkedAt: Long
)

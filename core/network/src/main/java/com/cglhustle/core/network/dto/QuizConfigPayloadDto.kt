package com.cglhustle.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizConfigPayloadDto(
    @SerialName("subject") val subject: String,
    @SerialName("topic") val topic: String,
    @SerialName("difficulty") val difficulty: String,
    @SerialName("exam_year") val examYear: String,
    @SerialName("shift") val shift: String
)

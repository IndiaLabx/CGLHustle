package com.cglhustle.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizConfigPayloadDto(
    @SerialName("subject") val subject: String,
    @SerialName("topic") val topic: String,
    @SerialName("sub_topic") val subTopic: String? = null,
    @SerialName("difficulty") val difficulty: String,
    @SerialName("exam_year") val examYear: String,
    @SerialName("shift") val shift: String,
    @SerialName("mode") val mode: String = "LEARNING",
    @SerialName("question_count") val questionCount: Int = 10,
    @SerialName("question_type") val questionType: String = "MCQ",
    @SerialName("quiz_name") val quizName: String? = null
)

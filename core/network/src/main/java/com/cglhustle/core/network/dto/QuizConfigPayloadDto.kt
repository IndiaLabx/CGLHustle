package com.cglhustle.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizConfigPayloadDto(
    @SerialName("subjects") val subjects: List<String> = emptyList(),
    @SerialName("topics") val topics: List<String> = emptyList(),
    @SerialName("sub_topics") val subTopics: List<String> = emptyList(),
    @SerialName("difficulty") val difficulty: String,
    @SerialName("exam_names") val examNames: List<String> = emptyList(),
    @SerialName("exam_years") val examYears: List<String> = emptyList(),
    @SerialName("shifts") val shifts: List<String> = emptyList(),
    @SerialName("tags") val tags: List<String> = emptyList(),
    @SerialName("mode") val mode: String = "LEARNING",
    @SerialName("question_count") val questionCount: Int = 10,
    @SerialName("question_type") val questionType: String = "MCQ",
    @SerialName("quiz_name") val quizName: String? = null
)

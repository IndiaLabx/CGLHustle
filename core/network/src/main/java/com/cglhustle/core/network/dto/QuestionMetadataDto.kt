package com.cglhustle.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionMetadataDto(
    @SerialName("id") val id: String,
    @SerialName("subject") val subject: String? = null,
    @SerialName("topic") val topic: String? = null,
    @SerialName("sub_topic") val subTopic: String? = null,
    @SerialName("difficulty") val difficulty: String? = null,
    @SerialName("question_type") val questionType: String? = null,
    @SerialName("exam_name") val examName: String? = null,
    @SerialName("exam_year") val examYear: String? = null,
    @SerialName("tags") val tags: List<String>? = null
)

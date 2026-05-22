package com.cglhustle.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionMetadataDto(
    @SerialName("id") val id: String,
    @SerialName("subject") val subject: String? = null,
    @SerialName("topic") val topic: String? = null,
    @SerialName("subTopic") val subTopic: String? = null,
    @SerialName("difficulty") val difficulty: String? = null,
    @SerialName("questionType") val questionType: String? = null,
    @SerialName("examName") val examName: String? = null,
    @SerialName("examYear") val examYear: Int? = null,
    @SerialName("tags") val tags: List<String>? = null
)

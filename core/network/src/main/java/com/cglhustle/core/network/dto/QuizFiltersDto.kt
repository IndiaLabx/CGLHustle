package com.cglhustle.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizFiltersDto(
    @SerialName("subjects") val subjects: List<String>,
    @SerialName("topics") val topics: List<String>,
    @SerialName("difficulties") val difficulties: List<String>,
    @SerialName("exam_years") val examYears: List<String>,
    @SerialName("shifts") val shifts: List<String>
)

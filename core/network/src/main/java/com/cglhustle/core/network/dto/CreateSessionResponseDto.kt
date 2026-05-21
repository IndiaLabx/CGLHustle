package com.cglhustle.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateSessionResponseDto(
    @SerialName("session") val session: QuizSessionStateDto,
    @SerialName("questions") val questions: List<QuestionSnapshotDto>
)

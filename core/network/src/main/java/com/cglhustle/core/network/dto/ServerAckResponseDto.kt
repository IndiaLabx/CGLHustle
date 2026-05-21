package com.cglhustle.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ServerAckStatus {
    @SerialName("applied") APPLIED,
    @SerialName("noop") NOOP,
    @SerialName("conflict") CONFLICT
}

@Serializable
data class ServerAckResponseDto(
    @SerialName("status") val status: ServerAckStatus,
    @SerialName("canonical_sequence") val canonicalSequence: Long
)

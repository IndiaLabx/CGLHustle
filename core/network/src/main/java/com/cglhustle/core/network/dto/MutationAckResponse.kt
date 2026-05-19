package com.cglhustle.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
enum class MutationStatus {
    APPLIED,
    NOOP,
    CONFLICT
}

@Serializable
data class MutationAckResponse(
    val status: MutationStatus,
    val canonicalSequence: Long
)

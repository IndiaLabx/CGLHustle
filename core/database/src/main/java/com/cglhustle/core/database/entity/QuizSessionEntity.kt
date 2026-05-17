package com.cglhustle.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SessionStatus {
    NOT_STARTED, IN_PROGRESS, PAUSED, SUBMITTED_LOCAL, SYNCED_FINAL, TERMINATED_CONFLICT, ABANDONED
}

@Entity(
    tableName = "quiz_sessions",
    indices = [
        Index(value = ["userId", "status", "updatedAt"])
    ]
)
data class QuizSessionEntity(
    @PrimaryKey
    val sessionId: String,
    val userId: String,
    val quizMetadataId: String,
    val status: SessionStatus,

    val startTime: Long?,
    val lastPausedTime: Long?,
    val endTime: Long?,
    val totalPausedDurationMs: Long,
    val activeDurationMs: Long,
    val currentQuestionId: String?,

    val sessionVersion: Int,
    val lastMutationId: String,
    val idempotencyKey: String,

    val deviceFingerprint: String?,
    val clientGeneratedAt: Long,
    val serverReceivedAt: Long?,
    val updatedAt: Long
)

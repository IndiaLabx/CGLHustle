package com.cglhustle.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class SessionStatus {
    NOT_STARTED, IN_PROGRESS, PAUSED, SUBMITTED_LOCAL, SYNCED_FINAL, TERMINATED_CONFLICT, ABANDONED
}

@Entity(tableName = "quiz_sessions")
data class QuizSessionEntity(
    @PrimaryKey
    val sessionId: UUID,
    val userId: UUID,
    val quizMetadataId: UUID,
    val status: SessionStatus,

    val startTime: Long?,
    val lastPausedTime: Long?,
    val endTime: Long?,
    val totalPausedDurationMs: Long,
    val activeDurationMs: Long,
    val currentQuestionId: UUID?,

    val sessionVersion: Int,
    val lastMutationId: UUID,
    val idempotencyKey: String,

    val deviceFingerprint: String?,
    val clientGeneratedAt: Long,
    val serverReceivedAt: Long?,
    val updatedAt: Long
)

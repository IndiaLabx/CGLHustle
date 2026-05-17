package com.cglhustle.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class AnswerMutationType {
    SELECT, CLEAR, MARK_REVIEW, UNMARK_REVIEW
}

@Entity(tableName = "user_answers")
data class UserAnswerEntity(
    @PrimaryKey
    val eventId: UUID,
    val supersedesEventId: UUID?,
    val mutationType: AnswerMutationType,

    val sessionId: UUID,
    val userId: UUID,
    val questionId: UUID,

    val selectedOption: String?,
    val isCorrect: Boolean?,
    val timeTakenSeconds: Double?,

    val attemptSequence: Int,
    val idempotencyKey: String,

    val clientGeneratedAt: Long,
    val serverReceivedAt: Long?,
    val updatedAt: Long
)

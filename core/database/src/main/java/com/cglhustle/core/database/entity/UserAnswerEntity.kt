package com.cglhustle.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AnswerMutationType {
    SELECT, CLEAR, MARK_REVIEW, UNMARK_REVIEW
}

@Entity(
    tableName = "user_answers",
    indices = [
        Index(value = ["sessionId", "questionId"])
    ]
)
data class UserAnswerEntity(
    @PrimaryKey
    val eventId: String,
    val supersedesEventId: String?,
    val mutationType: AnswerMutationType,

    val sessionId: String,
    val userId: String,
    val questionId: String,

    val selectedOption: String?,
    val isCorrect: Boolean?,
    val timeTakenSeconds: Double?,

    val attemptSequence: Int,
    val idempotencyKey: String,

    val clientGeneratedAt: Long,
    val serverReceivedAt: Long?,
    val updatedAt: Long
)

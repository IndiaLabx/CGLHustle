package com.cglhustle.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "question_snapshots")
data class QuestionSnapshotEntity(
    @PrimaryKey
    val id: UUID,
    val quizSessionId: UUID,
    val contentVersion: Int,
    val snapshotHash: String,
    val sourceProject: String, // e.g., 'gk_llm'
    val sourceFetchedAt: Long,
    val languagePackVersion: String,
    val isDeletedUpstream: Boolean,

    val subject: String,
    val topic: String,
    val difficulty: String,
    val questionType: String,

    val questionText: String,
    val questionTextHi: String?,

    // Stored as JSON strings
    val options: String,
    val optionsHi: String?,

    val correctAnswer: String,
    val explanation: String?,
    val tags: String? // JSON list
)

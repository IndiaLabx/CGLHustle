package com.cglhustle.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "question_snapshots",
    indices = [
        Index(value = ["quizSessionId", "questionId"], unique = true)
    ]
)
data class QuestionSnapshotEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val quizSessionId: String,
    val questionId: String,
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
    val options: List<String>,
    val optionsHi: List<String>?,

    val correctAnswer: String,
    val explanation: Map<String, String>?,
    val tags: List<String>? // JSON list
)

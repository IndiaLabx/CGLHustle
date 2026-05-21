package com.cglhustle.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionSnapshotDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("quiz_session_id") val quizSessionId: String,
    @SerialName("question_id") val questionId: String,
    @SerialName("content_version") val contentVersion: Int,
    @SerialName("snapshot_hash") val snapshotHash: String,
    @SerialName("source_project") val sourceProject: String,
    @SerialName("source_fetched_at") val sourceFetchedAt: Long,
    @SerialName("language_pack_version") val languagePackVersion: String,
    @SerialName("is_deleted_upstream") val isDeletedUpstream: Boolean,
    @SerialName("subject") val subject: String,
    @SerialName("topic") val topic: String,
    @SerialName("difficulty") val difficulty: String,
    @SerialName("question_type") val questionType: String,
    @SerialName("question_text") val questionText: String,
    @SerialName("question_text_hi") val questionTextHi: String?,
    @SerialName("options") val options: List<String>,
    @SerialName("options_hi") val optionsHi: List<String>?,
    @SerialName("correct_answer") val correctAnswer: String,
    @SerialName("explanation") val explanation: Map<String, String>?,
    @SerialName("tags") val tags: List<String>?
)

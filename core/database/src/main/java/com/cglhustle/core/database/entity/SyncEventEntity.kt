package com.cglhustle.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SyncStatus {
    PENDING, PENDING_AUTH, IN_FLIGHT, ACKED, RESOLVED_DROPPED, FAILED_RETRY, FAILED_FATAL
}

enum class SyncEventType {
    UPSERT_SESSION, UPSERT_ANSWER, MARK_COMPLETED
}

@Entity(
    tableName = "sync_events",
    indices = [
        Index(value = ["userId", "idempotencyKey"], unique = true),
        Index(value = ["status", "createdAt"]) // Added missing index
    ]
)
data class SyncEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val idempotencyKey: String,
    val eventType: SyncEventType,
    val payload: String, // JSON
    val status: SyncStatus,

    val createdAt: Long,
    val nextRetryAt: Long?,
    val retryCount: Int,
    val lastErrorCode: String?,
    val lastErrorAt: Long?,

    val lastAttemptAt: Long? = null,
    val processingToken: String? = null
)

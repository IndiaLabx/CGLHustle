package com.cglhustle.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cglhustle.core.database.entity.SyncEventEntity
import com.cglhustle.core.database.entity.SyncStatus

@Dao
abstract class SyncEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertEvent(event: SyncEventEntity): Long

    @Query("SELECT * FROM sync_events WHERE status IN (:statuses) ORDER BY createdAt ASC LIMIT :limit")
    abstract suspend fun getPendingEvents(
        statuses: List<SyncStatus> = listOf(SyncStatus.PENDING, SyncStatus.FAILED_RETRY),
        limit: Int = 50
    ): List<SyncEventEntity>

    @Update
    abstract suspend fun updateEvent(event: SyncEventEntity)

    @Query("UPDATE sync_events SET status = :status WHERE id = :id")
    abstract suspend fun updateStatus(id: Long, status: SyncStatus)

    @Query("UPDATE sync_events SET status = :status, processingToken = :processingToken, lastAttemptAt = :lastAttemptAt WHERE id = :id")
    abstract suspend fun updateEventCheckpoint(id: Long, status: SyncStatus, processingToken: String?, lastAttemptAt: Long)

    @Query("DELETE FROM sync_events WHERE status IN (:statuses)")
    abstract suspend fun deleteEventsWithStatus(statuses: List<SyncStatus>)

    @Query("SELECT COUNT(*) FROM sync_events WHERE userId = :userId AND idempotencyKey = :idempotencyKey")
    abstract suspend fun checkEventExists(userId: String, idempotencyKey: String): Int

    // Stale recovery: IN_FLIGHT older than 15 minutes to FAILED_RETRY
    @Query("UPDATE sync_events SET status = :newStatus, nextRetryAt = :now + 30000 WHERE status = :currentStatus AND lastAttemptAt < :thresholdTime")
    abstract suspend fun recoverStaleEvents(
        thresholdTime: Long,
        now: Long,
        currentStatus: SyncStatus = SyncStatus.IN_FLIGHT,
        newStatus: SyncStatus = SyncStatus.FAILED_RETRY
    ): Int

    // Claim rows: PENDING or FAILED_RETRY -> IN_FLIGHT and set processing token
    @Query("UPDATE sync_events SET status = :newStatus, processingToken = :token, lastAttemptAt = :now WHERE id IN (:ids)")
    abstract suspend fun claimEvents(
        ids: List<Long>,
        token: String,
        now: Long,
        newStatus: SyncStatus = SyncStatus.IN_FLIGHT
    )

    // Revert unprocessed rows: IN_FLIGHT -> PENDING and clear token
    @Query("UPDATE sync_events SET status = :newStatus, processingToken = NULL WHERE status = :currentStatus AND processingToken = :token")
    abstract suspend fun revertUnprocessedEvents(
        token: String,
        currentStatus: SyncStatus = SyncStatus.IN_FLIGHT,
        newStatus: SyncStatus = SyncStatus.PENDING
    ): Int

    @Query("SELECT COUNT(*) FROM sync_events WHERE status IN (:statuses)")
    abstract suspend fun countEventsWithStatus(statuses: List<SyncStatus>): Int
}

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

    @Query("DELETE FROM sync_events WHERE status IN (:statuses)")
    abstract suspend fun deleteEventsWithStatus(statuses: List<SyncStatus>)

    @Query("SELECT COUNT(*) FROM sync_events WHERE userId = :userId AND idempotencyKey = :idempotencyKey")
    abstract suspend fun checkEventExists(userId: String, idempotencyKey: String): Int
}

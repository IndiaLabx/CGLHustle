package com.cglhustle.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cglhustle.core.database.entity.SyncEventEntity
import com.cglhustle.core.database.entity.SyncStatus

@Dao
interface SyncEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SyncEventEntity): Long

    @Query("SELECT * FROM sync_events WHERE status IN (:statuses) ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getPendingEvents(
        statuses: List<SyncStatus> = listOf(SyncStatus.PENDING, SyncStatus.FAILED_RETRY),
        limit: Int = 50
    ): List<SyncEventEntity>

    @Update
    suspend fun updateEvent(event: SyncEventEntity)

    @Query("UPDATE sync_events SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: SyncStatus)

    @Query("DELETE FROM sync_events WHERE status IN (:statuses)")
    suspend fun deleteEventsWithStatus(statuses: List<SyncStatus>)
}

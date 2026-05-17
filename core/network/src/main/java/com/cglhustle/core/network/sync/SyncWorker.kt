package com.cglhustle.core.network.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.cglhustle.core.database.dao.SyncEventDao
import com.cglhustle.core.database.entity.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SyncWorker handles background synchronization using the Outbox pattern.
 * It reads events from the SyncEvent table and pushes them to the backend.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncEventDao: SyncEventDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch pending events
            val pendingEvents = syncEventDao.getPendingEvents(
                listOf(SyncStatus.PENDING, SyncStatus.FAILED_RETRY)
            )

            if (pendingEvents.isEmpty()) {
                return@withContext Result.success()
            }

            // 2. Iterate and process
            for (event in pendingEvents) {
                try {
                    // Mark as in-flight
                    syncEventDao.updateStatus(event.id, SyncStatus.IN_FLIGHT)

                    // TODO: Push to Supabase backend based on event.eventType and event.payload
                    // Simulated success for Dry Run:

                    syncEventDao.updateStatus(event.id, SyncStatus.ACKED)

                } catch (e: Exception) {
                    // Simulated transient failure Handling
                    val updatedEvent = event.copy(
                        status = SyncStatus.FAILED_RETRY,
                        retryCount = event.retryCount + 1,
                        lastErrorCode = e.message,
                        lastErrorAt = System.currentTimeMillis()
                    )
                    syncEventDao.updateEvent(updatedEvent)
                }
            }

            // Clean up successfully acked events periodically
            syncEventDao.deleteEventsWithStatus(listOf(SyncStatus.ACKED, SyncStatus.RESOLVED_DROPPED))

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

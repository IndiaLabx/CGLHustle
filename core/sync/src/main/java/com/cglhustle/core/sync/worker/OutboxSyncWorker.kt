package com.cglhustle.core.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.cglhustle.core.database.dao.SyncEventDao
import com.cglhustle.core.database.entity.SyncStatus
import com.cglhustle.core.sync.network.SyncNetworkDataSource
import com.cglhustle.core.sync.network.UnauthorizedException
import com.cglhustle.core.sync.orchestrator.SyncOrchestrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

@HiltWorker
class OutboxSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncEventDao: SyncEventDao,
    private val syncNetworkDataSource: SyncNetworkDataSource,
    private val syncOrchestrator: SyncOrchestrator
) : CoroutineWorker(context, params) {

    companion object {
        const val MAX_BATCH_SIZE = 50
        const val STALE_THRESHOLD_MS = 15 * 60 * 1000L // 15 minutes
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // 1. Check if Auth is Blocked
        val isBlocked = syncOrchestrator.isAuthBlocked.first()
        if (isBlocked) {
            return@withContext Result.success()
        }

        val now = System.currentTimeMillis()

        // 2. Stale Recovery
        // IN_FLIGHT rows older than 15 mins -> FAILED_RETRY
        syncEventDao.recoverStaleEvents(
            currentStatus = SyncStatus.IN_FLIGHT,
            newStatus = SyncStatus.FAILED_RETRY,
            thresholdTime = now - STALE_THRESHOLD_MS,
            now = now
        )

        // 3. Claim Batch Atomically
        val pendingEvents = syncEventDao.getPendingEvents(
            statuses = listOf(SyncStatus.PENDING, SyncStatus.FAILED_RETRY),
            limit = MAX_BATCH_SIZE
        )

        // Filter out events that haven't reached their nextRetryAt
        val eventsToProcess = pendingEvents.filter {
            it.nextRetryAt == null || (it.nextRetryAt ?: 0L) <= now
        }

        if (eventsToProcess.isEmpty()) {
            return@withContext Result.success()
        }

        val processingToken = UUID.randomUUID().toString()
        val eventIds = eventsToProcess.map { it.id }

        // Atomic claim: update processingToken and status = IN_FLIGHT for claimed IDs
        syncEventDao.claimEvents(
            ids = eventIds,
            token = processingToken,
            now = now,
            newStatus = SyncStatus.IN_FLIGHT
        )

        var encounteredTransientError = false

        for (event in eventsToProcess) {
            try {
                // Push to network
                syncNetworkDataSource.pushEvent(event)

                // On success, mark as ACKED
                syncEventDao.updateStatus(event.id, SyncStatus.ACKED)

            } catch (e: UnauthorizedException) {
                // On 401: Set AUTH_BLOCKED
                syncOrchestrator.setAuthBlocked(true)

                // Revert remaining unprocessed IN_FLIGHT rows with our token to PENDING
                syncEventDao.revertUnprocessedEvents(token = processingToken)

                return@withContext Result.success()

            } catch (e: Exception) {
                // On Transient Error: Mark event as FAILED_RETRY
                val backoffMultiplier = Math.pow(2.0, event.retryCount.toDouble()).toLong()
                val nextRetry = now + (30000 * backoffMultiplier) // e.g. 30s base backoff
                val updatedEvent = event.copy(
                    status = SyncStatus.FAILED_RETRY,
                    retryCount = event.retryCount + 1,
                    lastErrorCode = e.message,
                    lastErrorAt = now,
                    nextRetryAt = nextRetry,
                    processingToken = null
                )
                syncEventDao.updateEvent(updatedEvent)

                // Revert remaining unprocessed IN_FLIGHT rows with our token to PENDING
                syncEventDao.revertUnprocessedEvents(token = processingToken)

                encounteredTransientError = true
                break // Halt batch immediately
            }
        }

        // Clean up successfully acked events periodically
        syncEventDao.deleteEventsWithStatus(listOf(SyncStatus.ACKED, SyncStatus.RESOLVED_DROPPED))

        if (encounteredTransientError) {
            return@withContext Result.retry()
        }

        // Check if more pending events remain
        val pendingCount = syncEventDao.countEventsWithStatus(listOf(SyncStatus.PENDING, SyncStatus.FAILED_RETRY))
        if (pendingCount > 0) {
            // Re-enqueue for next batch
            syncOrchestrator.enqueueSync()
        }

        Result.success()
    }
}

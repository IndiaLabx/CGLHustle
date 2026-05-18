package com.cglhustle.core.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cglhustle.core.common.error.Failure
import com.cglhustle.core.common.error.NetworkError
import com.cglhustle.core.common.error.Success
import com.cglhustle.core.database.dao.SyncEventDao
import com.cglhustle.core.database.entity.SyncStatus
import com.cglhustle.core.network.SyncNetworkDataSource
import com.cglhustle.core.sync.orchestrator.SyncOrchestrator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.UUID

@HiltWorker
class OutboxSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncEventDao: SyncEventDao,
    private val syncNetworkDataSource: SyncNetworkDataSource,
    private val syncOrchestrator: SyncOrchestrator
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val pendingEvents = syncEventDao.getPendingEvents()

        if (pendingEvents.isEmpty()) {
            return Result.success()
        }

        val processingToken = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        syncEventDao.claimEvents(
            ids = pendingEvents.map { it.id },
            token = processingToken,
            now = now
        )

        var hasFailure = false

        try {
            for (event in pendingEvents) {
                if (isStopped) {
                    break
                }

                val result = syncNetworkDataSource.pushEvent(event)
                when (result) {
                    is Success -> {
                        syncEventDao.updateEventCheckpoint(
                            id = event.id,
                            status = SyncStatus.ACKED,
                            processingToken = null,
                            lastAttemptAt = System.currentTimeMillis()
                        )
                    }
                    is Failure -> {
                        when (result.error) {
                            is NetworkError.AuthExpired -> {
                                syncOrchestrator.setAuthBlocked(true)
                                break // Halt batch
                            }
                            else -> {
                                hasFailure = true
                                syncEventDao.updateEventCheckpoint(
                                    id = event.id,
                                    status = SyncStatus.FAILED_RETRY,
                                    processingToken = null,
                                    lastAttemptAt = System.currentTimeMillis()
                                )
                            }
                        }
                    }
                }
            }
        } finally {
            syncEventDao.revertUnprocessedEvents(processingToken)
        }

        return when {
            hasFailure -> Result.retry()
            else -> Result.success()
        }
    }
}

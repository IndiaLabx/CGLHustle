package com.cglhustle.core.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cglhustle.core.common.error.Failure
import com.cglhustle.core.common.error.NetworkError
import com.cglhustle.core.common.error.Success
import com.cglhustle.core.common.logging.LogLevel
import com.cglhustle.core.common.logging.StructuredLogger
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
    private val syncOrchestrator: SyncOrchestrator,
    private val logger: StructuredLogger
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val pendingEvents = syncEventDao.getPendingEvents()

        if (pendingEvents.isEmpty()) {
            logger.log(
                level = LogLevel.DEBUG,
                module = "OutboxSyncWorker",
                event = "No pending events, sync completed"
            )
            return Result.success()
        }

        val processingToken = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        logger.log(
            level = LogLevel.INFO,
            module = "OutboxSyncWorker",
            event = "Starting sync batch",
            correlationId = processingToken,
            payload = "{\"eventCount\": ${pendingEvents.size}}"
        )

        syncEventDao.claimEvents(
            ids = pendingEvents.map { it.id },
            token = processingToken,
            now = now
        )

        pendingEvents.forEach {
            logger.log(
                level = LogLevel.DEBUG,
                module = "OutboxSyncWorker",
                event = "Transition PENDING -> IN_FLIGHT",
                correlationId = processingToken,
                payload = "{\"eventId\": \"${it.id}\"}"
            )
        }

        var hasFailure = false

        try {
            for (event in pendingEvents) {
                if (isStopped) {
                    logger.log(
                        level = LogLevel.WARN,
                        module = "OutboxSyncWorker",
                        event = "Worker stopped, aborting batch",
                        correlationId = processingToken
                    )
                    break
                }

                val result = syncNetworkDataSource.pushEvent(event)
                when (result) {
                    is Success -> {
                        logger.log(
                            level = LogLevel.INFO,
                            module = "OutboxSyncWorker",
                            event = "Transition IN_FLIGHT -> ACKED",
                            correlationId = processingToken,
                            payload = "{\"eventId\": \"${event.id}\"}"
                        )
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
                                logger.log(
                                    level = LogLevel.ERROR,
                                    module = "OutboxSyncWorker",
                                    event = "Auth expired, blocking sync",
                                    correlationId = processingToken
                                )
                                syncOrchestrator.setAuthBlocked(true)
                                break // Halt batch
                            }
                            else -> {
                                hasFailure = true
                                logger.log(
                                    level = LogLevel.WARN,
                                    module = "OutboxSyncWorker",
                                    event = "Transition IN_FLIGHT -> FAILED_RETRY",
                                    correlationId = processingToken,
                                    payload = "{\"eventId\": \"${event.id}\", \"error\": \"${result.error.telemetryCode}\"}"
                                )
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
            logger.log(
                level = LogLevel.DEBUG,
                module = "OutboxSyncWorker",
                event = "Reverting unprocessed events",
                correlationId = processingToken
            )
            syncEventDao.revertUnprocessedEvents(processingToken)
        }

        return when {
            hasFailure -> {
                logger.log(
                    level = LogLevel.INFO,
                    module = "OutboxSyncWorker",
                    event = "Sync batch completed with failures",
                    correlationId = processingToken
                )
                Result.retry()
            }
            else -> {
                logger.log(
                    level = LogLevel.INFO,
                    module = "OutboxSyncWorker",
                    event = "Sync batch completed successfully",
                    correlationId = processingToken
                )
                Result.success()
            }
        }
    }
}

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
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class OutboxSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncEventDao: SyncEventDao,
    private val syncNetworkDataSource: SyncNetworkDataSource,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val pendingEvents = syncEventDao.getPendingEvents()

        if (pendingEvents.isEmpty()) {
            return Result.success()
        }

        var hasFailure = false
        var hasAuthError = false

        for (event in pendingEvents) {
            val result = syncNetworkDataSource.pushEvent(event)
            when (result) {
                is Success -> {
                    syncEventDao.updateStatus(event.id, SyncStatus.ACKED)
                }
                is Failure -> {
                    when (result.error) {
                        is NetworkError.AuthExpired -> {
                            hasAuthError = true
                            syncEventDao.updateStatus(event.id, SyncStatus.PENDING_AUTH)
                        }
                        else -> {
                            hasFailure = true
                        }
                    }
                }
            }
        }

        return when {
            hasAuthError -> Result.retry() // In a real app this would block the queue
            hasFailure -> Result.retry()
            else -> Result.success()
        }
    }
}

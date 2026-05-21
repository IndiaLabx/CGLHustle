package com.cglhustle.core.sync.network

import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.AppResult
import com.cglhustle.core.common.error.Failure
import com.cglhustle.core.common.error.NetworkError
import com.cglhustle.core.common.error.Success
import com.cglhustle.core.common.error.UnknownError
import com.cglhustle.core.database.entity.SyncEventEntity
import com.cglhustle.core.network.SyncNetworkDataSource

class FakeSyncNetworkDataSource : SyncNetworkDataSource {

    var shouldFailWithAuth = false
    var shouldFailWithConflict = false
    var shouldFailWithException = false

    val pushedEvents = mutableListOf<SyncEventEntity>()

    override suspend fun pushEvent(event: SyncEventEntity): AppResult<Unit, AppError> {
        if (shouldFailWithAuth) {
            return Failure(NetworkError.AuthExpired())
        }
        if (shouldFailWithConflict) {
            return Failure(NetworkError.Conflict())
        }
        if (shouldFailWithException) {
            return Failure(UnknownError())
        }
        pushedEvents.add(event)
        return Success(Unit)
    }
}

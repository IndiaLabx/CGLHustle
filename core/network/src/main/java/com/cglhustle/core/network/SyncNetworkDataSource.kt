package com.cglhustle.core.network

import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.AppResult
import com.cglhustle.core.database.entity.SyncEventEntity

interface SyncNetworkDataSource {
    suspend fun pushEvent(event: SyncEventEntity): AppResult<Unit, AppError>
}

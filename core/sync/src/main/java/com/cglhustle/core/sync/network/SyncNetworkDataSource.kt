package com.cglhustle.core.sync.network

import com.cglhustle.core.database.entity.SyncEventEntity

class UnauthorizedException(message: String) : Exception(message)

interface SyncNetworkDataSource {
    suspend fun pushEvent(event: SyncEventEntity)
}

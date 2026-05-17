package com.cglhustle.core.sync.network

import com.cglhustle.core.database.entity.SyncEventEntity

class FakeSyncNetworkDataSource : SyncNetworkDataSource {
    var shouldThrowUnauthorized = false
    var shouldThrowTransientError = false
    val pushedEvents = mutableListOf<SyncEventEntity>()

    override suspend fun pushEvent(event: SyncEventEntity) {
        if (shouldThrowUnauthorized) {
            throw UnauthorizedException("Simulated 401 Unauthorized")
        }
        if (shouldThrowTransientError) {
            throw Exception("Simulated Transient Network Error")
        }
        pushedEvents.add(event)
    }
}

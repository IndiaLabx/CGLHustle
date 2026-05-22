package com.cglhustle.core.network

import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.AppResult
import com.cglhustle.core.database.entity.SyncEventEntity
import com.cglhustle.core.network.dto.AnswerMutationRequest

interface SyncNetworkDataSource {
    suspend fun pushEvent(event: SyncEventEntity): AppResult<Unit, AppError>
    suspend fun submitAnswer(request: AnswerMutationRequest): AppResult<Unit, AppError>
}

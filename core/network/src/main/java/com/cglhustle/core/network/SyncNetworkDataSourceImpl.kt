package com.cglhustle.core.network

import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.AppResult
import com.cglhustle.core.common.error.Failure
import com.cglhustle.core.common.error.Success
import com.cglhustle.core.database.entity.SyncEventEntity
import com.cglhustle.core.network.dto.UserAnswerDto
import com.cglhustle.core.network.error.toAppError
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SyncNetworkDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient
) : SyncNetworkDataSource {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun pushEvent(event: SyncEventEntity): AppResult<Unit, AppError> {
        return try {
            val payload = json.decodeFromString<UserAnswerDto>(event.payload)

            httpClient.post("/rest/v1/rpc/upsert_user_answer_safe") {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }

            Success(Unit)
        } catch (e: Exception) {
            Failure(e.toAppError())
        }
    }
}

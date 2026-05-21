package com.cglhustle.core.network

import com.cglhustle.core.network.dto.AnswerMutationRequest
import com.cglhustle.core.network.dto.ServerAckResponseDto
import com.cglhustle.core.network.dto.QuizSessionStateDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class CglHustleApi @Inject constructor(
    private val httpClient: HttpClient
) {
    suspend fun getQuizSessionState(sessionId: String): QuizSessionStateDto {
        return httpClient.get("/rest/v1/quiz_sessions?session_id=eq.$sessionId").body<List<QuizSessionStateDto>>().first()
    }

    suspend fun submitAnswerMutation(request: AnswerMutationRequest): ServerAckResponseDto {
        return httpClient.post("/rest/v1/rpc/upsert_user_answer_safe") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}

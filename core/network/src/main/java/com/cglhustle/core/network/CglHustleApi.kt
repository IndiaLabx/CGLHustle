package com.cglhustle.core.network

import com.cglhustle.core.config.PrimaryBackendHttpClient
import com.cglhustle.core.network.dto.AnswerMutationRequest
import com.cglhustle.core.network.dto.ServerAckResponseDto
import com.cglhustle.core.network.dto.QuizSessionStateDto
import com.cglhustle.core.network.dto.QuizFiltersDto
import com.cglhustle.core.network.dto.QuizConfigPayloadDto
import com.cglhustle.core.network.dto.CreateSessionResponseDto
import com.cglhustle.core.network.dto.ResultsAnalyticsDto
import com.cglhustle.core.network.dto.AttemptedQuestionDto
import com.cglhustle.core.network.dto.BookmarkedQuestionDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class CglHustleApi @Inject constructor(
    @PrimaryBackendHttpClient private val httpClient: HttpClient
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

    suspend fun getQuizFilters(): QuizFiltersDto {
        return httpClient.get("/rest/v1/rpc/get_quiz_filters").body()
    }

    suspend fun createQuizSession(payload: QuizConfigPayloadDto): CreateSessionResponseDto {
        return httpClient.post("/rest/v1/rpc/create_quiz_session") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }.body()
    }

    suspend fun getQuizResult(sessionId: String): ResultsAnalyticsDto {
        return httpClient.get("/rest/v1/rpc/get_quiz_result?session_id=$sessionId").body()
    }

    suspend fun getAttemptedQuestions(sessionId: String): List<AttemptedQuestionDto> {
        return httpClient.get("/rest/v1/rpc/get_attempted_questions?session_id=$sessionId").body()
    }

    suspend fun getBookmarks(): List<BookmarkedQuestionDto> {
        return httpClient.get("/rest/v1/rpc/get_bookmarks").body()
    }
}

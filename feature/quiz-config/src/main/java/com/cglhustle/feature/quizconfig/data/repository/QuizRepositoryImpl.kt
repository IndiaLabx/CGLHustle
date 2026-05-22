package com.cglhustle.feature.quizconfig.data.repository

import com.cglhustle.core.config.PrimaryBackendHttpClient
import com.cglhustle.feature.quizconfig.domain.repository.QuizRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class CreateSavedQuizRequest(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("mode") val mode: String,
    @SerialName("filters") val filters: String,
    @SerialName("status") val status: String
)

@Serializable
data class BridgeQuizQuestionRequest(
    @SerialName("quiz_id") val quizId: String,
    @SerialName("question_id") val questionId: String,
    @SerialName("sort_order") val sortOrder: Int
)

@Singleton
class QuizRepositoryImpl @Inject constructor(
    @PrimaryBackendHttpClient private val httpClient: HttpClient
) : QuizRepository {

    override suspend fun createQuiz(
        quizName: String,
        mode: String,
        filters: String,
        questionIds: List<String>
    ): String {
        val quizId = UUID.randomUUID().toString()

        val createQuizRequest = CreateSavedQuizRequest(
            id = quizId,
            name = quizName,
            mode = mode,
            filters = filters, // JSON map converted to string
            status = "active"
        )

        // 1. Insert into saved_quizzes
        httpClient.post("/rest/v1/saved_quizzes") {
            contentType(ContentType.Application.Json)
            setBody(createQuizRequest)
        }

        // 2. Batch insert into bridge_saved_quiz_questions
        val bridgeRequests = questionIds.mapIndexed { index, qId ->
            BridgeQuizQuestionRequest(
                quizId = quizId,
                questionId = qId,
                sortOrder = index
            )
        }

        httpClient.post("/rest/v1/bridge_saved_quiz_questions") {
            contentType(ContentType.Application.Json)
            setBody(bridgeRequests)
        }

        return quizId
    }
}

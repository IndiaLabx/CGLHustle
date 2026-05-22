package com.cglhustle.feature.mcqs.data.repository

import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.AppResult
import com.cglhustle.core.common.error.Failure
import com.cglhustle.core.common.error.Success
import com.cglhustle.core.config.QuestionBackendHttpClient
import com.cglhustle.core.network.dto.QuestionSnapshotDto
import com.cglhustle.core.network.error.toAppError
import com.cglhustle.feature.mcqs.domain.repository.QuestionRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionRepositoryImpl @Inject constructor(
    @QuestionBackendHttpClient private val questionHttpClient: HttpClient
) : QuestionRepository {

    override suspend fun fetchQuestionsForQuiz(quizId: String): AppResult<List<QuestionSnapshotDto>, AppError> {
        return try {
            // Note: Explicitly fetching from GK LLM endpoint structure.
            // Replace with actual Supabase REST/RPC queries.
            val questions: List<QuestionSnapshotDto> = questionHttpClient.get("/rest/v1/questions?quiz_id=eq.$quizId").body()
            Success(questions)
        } catch (e: Exception) {
            Failure(e.toAppError())
        }
    }
}

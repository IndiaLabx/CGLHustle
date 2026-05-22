package com.cglhustle.feature.mcqs.domain.repository

import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.AppResult
import com.cglhustle.core.network.dto.QuestionSnapshotDto

/**
 * STRICT RULE:
 * This repository is strictly tied to the GK LLM Backend.
 *
 * ALLOWED:
 * - fetch questions
 * - search questions
 * - paginate questions
 * - filter questions
 *
 * FORBIDDEN:
 * - Mutate data
 * - Persist session state
 * - Authentication operations
 */
interface QuestionRepository {

    suspend fun fetchQuestionsForQuiz(quizId: String): AppResult<List<QuestionSnapshotDto>, AppError>

}

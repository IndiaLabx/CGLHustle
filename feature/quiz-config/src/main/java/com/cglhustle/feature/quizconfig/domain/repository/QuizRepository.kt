package com.cglhustle.feature.quizconfig.domain.repository

interface QuizRepository {
    suspend fun createQuiz(
        quizName: String,
        mode: String,
        filters: String, // JSON string
        questionIds: List<String>
    ): String
}

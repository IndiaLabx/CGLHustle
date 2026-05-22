package com.cglhustle.feature.quizconfig.domain.model

data class QuestionMetadata(
    val id: String,
    val subject: String,
    val topic: String,
    val subTopic: String,
    val difficulty: String,
    val questionType: String,
    val examName: String,
    val examYear: String,
    val tags: List<String>
)

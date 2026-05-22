package com.cglhustle.feature.quizconfig.domain.repository

import com.cglhustle.feature.quizconfig.domain.model.QuestionMetadata

interface QuestionMetadataRepository {
    suspend fun fetchMetadata(): List<QuestionMetadata>
}

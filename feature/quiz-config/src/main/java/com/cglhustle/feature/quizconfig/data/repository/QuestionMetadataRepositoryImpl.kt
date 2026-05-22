package com.cglhustle.feature.quizconfig.data.repository

import com.cglhustle.core.config.QuestionBackendHttpClient
import com.cglhustle.core.network.dto.QuestionMetadataDto
import com.cglhustle.feature.quizconfig.domain.model.QuestionMetadata
import com.cglhustle.feature.quizconfig.domain.repository.QuestionMetadataRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionMetadataRepositoryImpl @Inject constructor(
    @QuestionBackendHttpClient private val httpClient: HttpClient
) : QuestionMetadataRepository {

    override suspend fun fetchMetadata(): List<QuestionMetadata> {
        val dtos: List<QuestionMetadataDto> = httpClient.get("/rest/v1/questions?select=id,subject,topic,sub_topic,difficulty,question_type,exam_name,exam_year,tags").body()
        return dtos.map {
            QuestionMetadata(
                id = it.id,
                subject = it.subject ?: "",
                topic = it.topic ?: "",
                subTopic = it.subTopic ?: "",
                difficulty = it.difficulty ?: "",
                questionType = it.questionType ?: "",
                examName = it.examName ?: "",
                examYear = it.examYear ?: "",
                tags = it.tags ?: emptyList()
            )
        }
    }
}

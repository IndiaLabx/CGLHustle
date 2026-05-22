package com.cglhustle.feature.quizconfig.data.repository

import com.cglhustle.core.config.QuestionBackendHttpClient
import com.cglhustle.core.network.dto.QuestionMetadataDto
import com.cglhustle.feature.quizconfig.domain.model.QuestionMetadata
import com.cglhustle.feature.quizconfig.domain.repository.QuestionMetadataRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionMetadataRepositoryImpl @Inject constructor(
    @QuestionBackendHttpClient private val httpClient: HttpClient
) : QuestionMetadataRepository {

    override suspend fun fetchMetadata(): List<QuestionMetadata> {
        return try {
            val response = httpClient.get("/rest/v1/questions?select=id,subject,topic,subTopic,difficulty,questionType,examName,examYear,tags")

            if (response.status.isSuccess()) {
                val dtos: List<QuestionMetadataDto> = response.body()
                dtos.map {
                    QuestionMetadata(
                        id = it.id,
                        subject = it.subject ?: "",
                        topic = it.topic ?: "",
                        subTopic = it.subTopic ?: "",
                        difficulty = it.difficulty ?: "",
                        questionType = it.questionType ?: "",
                        examName = it.examName ?: "",
                        examYear = it.examYear?.toString() ?: "",
                        tags = it.tags ?: emptyList()
                    )
                }
            } else {
                // If it's a non-200 code, throw an exception so it is properly caught upstream
                // rather than attempting to deserialize an error JSON payload into a List.
                throw Exception("Failed to fetch metadata. Server responded with: ${response.status}")
            }
        } catch (e: ClientRequestException) {
            // Log in debug if necessary
            throw Exception("Failed to fetch metadata due to client request issue.")
        } catch (e: ServerResponseException) {
            // Log in debug if necessary
            throw Exception("Failed to fetch metadata due to server response issue.")
        } catch (e: Exception) {
            // Log in debug if necessary
            throw Exception("Unable to load quiz metadata. Please retry.")
        }
    }
}

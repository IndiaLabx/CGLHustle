package com.cglhustle.core.network.di

import com.cglhustle.core.config.BackendConfig
import com.cglhustle.core.config.PrimaryBackendHttpClient
import com.cglhustle.core.config.QuestionBackendHttpClient
import com.cglhustle.core.network.auth.AuthTokenProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @PrimaryBackendHttpClient
    fun providePrimaryHttpClient(authTokenProvider: AuthTokenProvider): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }

            install(Logging) {
                level = LogLevel.INFO
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15000L
                connectTimeoutMillis = 15000L
                socketTimeoutMillis = 15000L
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        val token = authTokenProvider.getLatestAccessToken()
                        if (token != null) {
                            io.ktor.client.plugins.auth.providers.BearerTokens(token, "")
                        } else null
                    }
                }
            }

            defaultRequest {
                val rawUrl = BackendConfig.primaryBackendUrl
                val cleanUrl = rawUrl.replace("https://", "").replace("http://", "")
                url {
                    protocol = if (rawUrl.startsWith("http://")) URLProtocol.HTTP else URLProtocol.HTTPS
                    host = cleanUrl
                }
                header("apikey", BackendConfig.primaryAnonKey)
            }
        }
    }

    @Provides
    @Singleton
    @QuestionBackendHttpClient
    fun provideQuestionHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }

            install(Logging) {
                level = LogLevel.INFO
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15000L
                connectTimeoutMillis = 15000L
                socketTimeoutMillis = 15000L
            }

            // INTENTIONALLY OMITTED: NO Auth Plugin Here. Only READ access headers.

            defaultRequest {
                val rawUrl = BackendConfig.questionBackendUrl
                val cleanUrl = rawUrl.replace("https://", "").replace("http://", "")
                url {
                    protocol = if (rawUrl.startsWith("http://")) URLProtocol.HTTP else URLProtocol.HTTPS
                    host = cleanUrl
                }
                header("apikey", BackendConfig.questionAnonKey)
            }
        }
    }
}

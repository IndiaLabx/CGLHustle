package com.cglhustle.core.network.auth

interface AuthTokenProvider {
    suspend fun getLatestAccessToken(): String?
}

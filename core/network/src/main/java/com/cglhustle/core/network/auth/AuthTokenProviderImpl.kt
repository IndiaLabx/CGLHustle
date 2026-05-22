package com.cglhustle.core.network.auth

import com.cglhustle.core.config.PrimaryBackend
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenProviderImpl @Inject constructor(
    @PrimaryBackend private val supabaseClient: SupabaseClient
) : AuthTokenProvider {
    override suspend fun getLatestAccessToken(): String? {
        return supabaseClient.auth.currentAccessTokenOrNull()
    }
}

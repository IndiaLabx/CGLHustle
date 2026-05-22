package com.cglhustle.core.network.auth

import com.cglhustle.core.config.PrimaryBackend
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @PrimaryBackend private val supabaseClient: SupabaseClient
) : AuthRepository {

    override val sessionStatus: StateFlow<SessionStatus> = supabaseClient.auth.sessionStatus

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = runCatching {
        supabaseClient.auth.signInWith(io.github.jan.supabase.gotrue.providers.builtin.IDToken) {
            this.idToken = idToken
            this.provider = Google
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> = runCatching {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String, name: String): Result<Unit> = runCatching {
        supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        supabaseClient.auth.signOut()
    }
}

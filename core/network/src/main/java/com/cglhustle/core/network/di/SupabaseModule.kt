package com.cglhustle.core.network.di

import com.cglhustle.core.config.BackendConfig
import com.cglhustle.core.config.PrimaryBackend
import com.cglhustle.core.config.QuestionBackend
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    @PrimaryBackend
    fun providePrimarySupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BackendConfig.primaryBackendUrl,
            supabaseKey = BackendConfig.primaryAnonKey
        ) {
            install(Auth)
            install(Postgrest)
        }
    }

    @Provides
    @Singleton
    @QuestionBackend
    fun provideQuestionSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BackendConfig.questionBackendUrl,
            supabaseKey = BackendConfig.questionAnonKey
        ) {
            // ONLY postgrest is allowed for reading. No Auth!
            install(Postgrest)
        }
    }
}

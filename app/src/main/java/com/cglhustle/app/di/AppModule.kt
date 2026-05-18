package com.cglhustle.app.di

import com.cglhustle.core.network.auth.AuthTokenProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthTokenProvider(): AuthTokenProvider {
        return object : AuthTokenProvider {
            override suspend fun getLatestAccessToken(): String? = "dummy_token"
        }
    }
}

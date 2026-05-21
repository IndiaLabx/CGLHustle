package com.cglhustle.core.network.di

import com.cglhustle.core.network.auth.AuthRepository
import com.cglhustle.core.network.auth.AuthRepositoryImpl
import com.cglhustle.core.network.auth.AuthTokenProvider
import com.cglhustle.core.network.auth.AuthTokenProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthTokenProvider(
        impl: AuthTokenProviderImpl
    ): AuthTokenProvider

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}

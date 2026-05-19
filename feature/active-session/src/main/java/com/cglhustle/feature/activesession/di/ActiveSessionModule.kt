package com.cglhustle.feature.activesession.di

import com.cglhustle.feature.activesession.data.ActiveSessionRepositoryImpl
import com.cglhustle.feature.activesession.domain.ActiveSessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ActiveSessionModule {

    @Binds
    @Singleton
    abstract fun bindActiveSessionRepository(
        impl: ActiveSessionRepositoryImpl
    ): ActiveSessionRepository
}

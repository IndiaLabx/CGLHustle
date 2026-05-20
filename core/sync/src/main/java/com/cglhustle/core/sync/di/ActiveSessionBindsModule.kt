package com.cglhustle.core.sync.di

import com.cglhustle.core.sync.domain.ActiveSessionRepositoryImpl
import com.cglhustle.feature.activesession.domain.ActiveSessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ActiveSessionBindsModule {

    @Binds
    @Singleton
    abstract fun bindActiveSessionRepository(
        impl: ActiveSessionRepositoryImpl
    ): ActiveSessionRepository
}

package com.cglhustle.feature.activesession.di

import com.cglhustle.feature.activesession.data.ActiveSessionRepositoryImpl
import com.cglhustle.feature.activesession.domain.ActiveSessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ActiveSessionModule {

    @Binds
    abstract fun bindActiveSessionRepository(
        impl: ActiveSessionRepositoryImpl
    ): ActiveSessionRepository
}

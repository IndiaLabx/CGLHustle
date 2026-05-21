package com.cglhustle.core.sync.di

import com.cglhustle.core.sync.domain.QuizConfigRepositoryImpl
import com.cglhustle.feature.quizconfig.domain.repository.QuizConfigRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class QuizConfigBindsModule {

    @Binds
    abstract fun bindQuizConfigRepository(
        impl: QuizConfigRepositoryImpl
    ): QuizConfigRepository
}

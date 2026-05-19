package com.cglhustle.feature.quizconfig.di

import com.cglhustle.feature.quizconfig.data.repository.QuizConfigRepositoryImpl
import com.cglhustle.feature.quizconfig.domain.repository.QuizConfigRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class QuizConfigModule {

    @Binds
    abstract fun bindQuizConfigRepository(
        impl: QuizConfigRepositoryImpl
    ): QuizConfigRepository
}

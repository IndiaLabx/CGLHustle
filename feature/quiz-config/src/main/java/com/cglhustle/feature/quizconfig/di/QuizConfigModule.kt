package com.cglhustle.feature.quizconfig.di

import com.cglhustle.feature.quizconfig.data.repository.QuestionMetadataRepositoryImpl
import com.cglhustle.feature.quizconfig.domain.repository.QuestionMetadataRepository
import com.cglhustle.feature.quizconfig.data.repository.QuizRepositoryImpl
import com.cglhustle.feature.quizconfig.domain.repository.QuizRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class QuizConfigModule {

    @Binds
    @Singleton
    abstract fun bindQuestionMetadataRepository(
        impl: QuestionMetadataRepositoryImpl
    ): QuestionMetadataRepository

    @Binds
    @Singleton
    abstract fun bindQuizRepository(
        impl: QuizRepositoryImpl
    ): QuizRepository
}

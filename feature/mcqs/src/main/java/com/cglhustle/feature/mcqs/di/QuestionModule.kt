package com.cglhustle.feature.mcqs.di

import com.cglhustle.feature.mcqs.data.repository.QuestionRepositoryImpl
import com.cglhustle.feature.mcqs.domain.repository.QuestionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class QuestionModule {

    @Binds
    abstract fun bindQuestionRepository(
        questionRepositoryImpl: QuestionRepositoryImpl
    ): QuestionRepository
}

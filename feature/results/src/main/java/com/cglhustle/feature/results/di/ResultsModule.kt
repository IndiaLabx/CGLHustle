package com.cglhustle.feature.results.di

import com.cglhustle.feature.results.data.repository.ResultsRepositoryImpl
import com.cglhustle.feature.results.domain.repository.ResultsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ResultsModule {

    @Binds
    @Singleton
    abstract fun bindResultsRepository(
        resultsRepositoryImpl: ResultsRepositoryImpl
    ): ResultsRepository
}

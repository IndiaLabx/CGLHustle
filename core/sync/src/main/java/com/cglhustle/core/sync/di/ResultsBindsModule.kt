package com.cglhustle.core.sync.di

import com.cglhustle.core.sync.domain.ResultsRepositoryImpl
import com.cglhustle.feature.results.domain.repository.ResultsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ResultsBindsModule {

    @Binds
    @Singleton
    abstract fun bindResultsRepository(
        resultsRepositoryImpl: ResultsRepositoryImpl
    ): ResultsRepository
}

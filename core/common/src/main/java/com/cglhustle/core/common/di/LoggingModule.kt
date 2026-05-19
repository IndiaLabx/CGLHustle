package com.cglhustle.core.common.di

import com.cglhustle.core.common.logging.LogcatStructuredLogger
import com.cglhustle.core.common.logging.StructuredLogger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LoggingModule {

    @Binds
    @Singleton
    abstract fun bindStructuredLogger(
        logger: LogcatStructuredLogger
    ): StructuredLogger
}

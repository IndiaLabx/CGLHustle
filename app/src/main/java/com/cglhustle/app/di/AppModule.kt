package com.cglhustle.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
// Feature teams must use @InstallIn(SingletonComponent::class) inside their own modules.
// Do not modify the central AppModule.
@InstallIn(SingletonComponent::class)
object AppModule {

}

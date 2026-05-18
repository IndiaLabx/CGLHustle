package com.cglhustle.core.network.di

import com.cglhustle.core.network.SyncNetworkDataSource
import com.cglhustle.core.network.SyncNetworkDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindsModule {

    @Binds
    @Singleton
    abstract fun bindSyncNetworkDataSource(
        impl: SyncNetworkDataSourceImpl
    ): SyncNetworkDataSource
}

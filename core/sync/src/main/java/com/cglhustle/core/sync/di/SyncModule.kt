package com.cglhustle.core.sync.di

import android.content.Context
import androidx.work.WorkManager
import com.cglhustle.core.sync.network.SyncNetworkDataSource
import com.cglhustle.core.sync.orchestrator.SyncOrchestrator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.cglhustle.core.database.entity.SyncEventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class DummySyncNetworkDataSource : SyncNetworkDataSource {
    override suspend fun pushEvent(event: SyncEventEntity) {
    }
}

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideSyncOrchestrator(
        @ApplicationContext context: Context,
        workManager: WorkManager
    ): SyncOrchestrator {
        return SyncOrchestrator(context, workManager)
    }

    @Provides
    @Singleton
    fun provideSyncNetworkDataSource(): SyncNetworkDataSource {
        return DummySyncNetworkDataSource()
    }
}

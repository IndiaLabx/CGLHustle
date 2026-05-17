package com.cglhustle.core.sync.orchestrator

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.cglhustle.core.sync.worker.OutboxSyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.syncDataStore by preferencesDataStore(name = "sync_prefs")

@Singleton
class SyncOrchestrator @Inject constructor(
    private val context: Context,
    private val workManager: WorkManager
) {
    companion object {
        val AUTH_BLOCKED_KEY = booleanPreferencesKey("auth_blocked")
        const val UNIQUE_WORK_NAME = "outbox_sync"
    }

    val isAuthBlocked: Flow<Boolean> = context.syncDataStore.data.map { preferences ->
        preferences[AUTH_BLOCKED_KEY] ?: false
    }

    suspend fun setAuthBlocked(blocked: Boolean) {
        context.syncDataStore.edit { preferences ->
            preferences[AUTH_BLOCKED_KEY] = blocked
        }
    }

    suspend fun resumeSync() {
        setAuthBlocked(false)
        enqueueSync()
    }

    fun enqueueSync() {
        val request = OneTimeWorkRequestBuilder<OutboxSyncWorker>().build()
        workManager.enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}

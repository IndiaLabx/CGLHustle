package com.cglhustle.core.sync.orchestrator

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.cglhustle.core.common.logging.LogLevel
import com.cglhustle.core.common.logging.StructuredLogger
import com.cglhustle.core.sync.worker.OutboxSyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.syncDataStore by preferencesDataStore(name = "sync_prefs")

@Singleton
class SyncOrchestrator @Inject constructor(
    private val context: Context,
    private val workManager: WorkManager,
    private val logger: StructuredLogger
) {
    companion object {
        val AUTH_BLOCKED_KEY = booleanPreferencesKey("auth_blocked")
        const val UNIQUE_WORK_NAME = "outbox_sync"
    }

    val isAuthBlocked: Flow<Boolean> = context.syncDataStore.data.map { preferences ->
        preferences[AUTH_BLOCKED_KEY] ?: false
    }

    suspend fun setAuthBlocked(blocked: Boolean) {
        logger.log(
            level = if (blocked) LogLevel.WARN else LogLevel.INFO,
            module = "SyncOrchestrator",
            event = if (blocked) "Auth Blocked" else "Auth Unblocked"
        )
        context.syncDataStore.edit { preferences ->
            preferences[AUTH_BLOCKED_KEY] = blocked
        }
    }

    suspend fun resumeSync() {
        logger.log(
            level = LogLevel.INFO,
            module = "SyncOrchestrator",
            event = "Resume Sync Initiated"
        )
        setAuthBlocked(false)
        enqueueSync()
    }

    fun enqueueSync() {
        logger.log(
            level = LogLevel.INFO,
            module = "SyncOrchestrator",
            event = "Enqueue Unique Sync Work"
        )
        val request = OneTimeWorkRequestBuilder<OutboxSyncWorker>().build()
        workManager.enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}

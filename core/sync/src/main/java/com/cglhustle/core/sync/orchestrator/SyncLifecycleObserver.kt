package com.cglhustle.core.sync.orchestrator

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.cglhustle.core.sync.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncLifecycleObserver @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
    @ApplicationScope private val applicationScope: CoroutineScope
) : DefaultLifecycleObserver {

    companion object {
        private const val TAG = "SyncLifecycleObserver"
        private const val DEBOUNCE_THRESHOLD_MS = 3000L
    }

    private var lastStopElapsedRealtimeMs: Long? = null
    internal val isRecoveryRunning = AtomicBoolean(false) // visible for testing

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d(TAG, "onStart trigger fired.")

        val now = SystemClock.elapsedRealtime()
        val lastStop = lastStopElapsedRealtimeMs

        if (lastStop != null && (now - lastStop) <= DEBOUNCE_THRESHOLD_MS) {
            Log.d(TAG, "Pipeline skipped due to debounce.")
            return
        }

        if (!isRecoveryRunning.compareAndSet(false, true)) {
            Log.d(TAG, "Pipeline skipped due to overlapping execution.")
            return
        }

        applicationScope.launch {
            try {
                val blocked = syncOrchestrator.isAuthBlocked.first()
                if (blocked) {
                    Log.d(TAG, "Pipeline skipped due to AUTH_BLOCKED.")
                } else {
                    syncOrchestrator.enqueueSync()
                    Log.d(TAG, "Enqueue success.")
                }
            } finally {
                isRecoveryRunning.set(false)
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        lastStopElapsedRealtimeMs = SystemClock.elapsedRealtime()
    }
}

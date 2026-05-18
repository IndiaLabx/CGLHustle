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

class SyncLifecycleObserver @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
    @ApplicationScope private val applicationScope: CoroutineScope
) : DefaultLifecycleObserver {

    companion object {
        const val TAG = "SyncLifecycleObserver"
        const val DEBOUNCE_THRESHOLD_MS = 3000L
    }

    private var lastStopElapsedRealtimeMs: Long = 0L
    private val isRecoveryRunning = AtomicBoolean(false)

    override fun onStop(owner: LifecycleOwner) {
        lastStopElapsedRealtimeMs = SystemClock.elapsedRealtime()
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.d(TAG, "onStart trigger fired.")

        val now = SystemClock.elapsedRealtime()

        // Cold start or greater than debounce threshold
        if (lastStopElapsedRealtimeMs != 0L && (now - lastStopElapsedRealtimeMs) <= DEBOUNCE_THRESHOLD_MS) {
            Log.d(TAG, "Pipeline skipped due to debounce.")
            return
        }

        if (!isRecoveryRunning.compareAndSet(false, true)) {
            return
        }

        applicationScope.launch {
            try {
                val isBlocked = syncOrchestrator.isAuthBlocked.first()
                if (isBlocked) {
                    Log.d(TAG, "Pipeline skipped due to AUTH_BLOCKED.")
                    return@launch
                }

                syncOrchestrator.enqueueSync()
                Log.d(TAG, "Enqueue success.")
            } finally {
                isRecoveryRunning.set(false)
            }
        }
    }
}

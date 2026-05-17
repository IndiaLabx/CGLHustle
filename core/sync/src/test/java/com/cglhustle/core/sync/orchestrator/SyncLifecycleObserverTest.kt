package com.cglhustle.core.sync.orchestrator

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowSystemClock
import java.time.Duration

class MockLifecycleOwner : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SyncLifecycleObserverTest {

    private lateinit var context: Context
    private lateinit var workManager: WorkManager
    private lateinit var syncOrchestrator: SyncOrchestrator
    private lateinit var testScope: TestScope
    private lateinit var observer: SyncLifecycleObserver
    private val lifecycleOwner = MockLifecycleOwner()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        workManager = WorkManager.getInstance(context)

        syncOrchestrator = SyncOrchestrator(context, workManager)
        testScope = TestScope(UnconfinedTestDispatcher())

        observer = SyncLifecycleObserver(syncOrchestrator, testScope)

        runBlocking {
            syncOrchestrator.setAuthBlocked(false)
        }

        ShadowSystemClock.advanceBy(Duration.ofMillis(100000))
        clearWorkManager()
    }

    private fun clearWorkManager() {
        workManager.cancelAllWork()
        workManager.pruneWork()
        ShadowSystemClock.advanceBy(Duration.ofMillis(100))
    }

    private fun getEnqueueCount(): Int {
        val infos = workManager.getWorkInfosForUniqueWork(SyncOrchestrator.UNIQUE_WORK_NAME).get()
        return infos.count { it.state == androidx.work.WorkInfo.State.ENQUEUED }
    }

    @Test
    fun `cold start triggers enqueueSync`() = runTest {
        observer.onStart(lifecycleOwner)
        ShadowSystemClock.advanceBy(Duration.ofMillis(100))
        assertEquals(1, getEnqueueCount())
    }

    @Test
    fun `onStart skips if debounce threshold not met`() = runTest {
        observer.onStart(lifecycleOwner)
        ShadowSystemClock.advanceBy(Duration.ofMillis(100))
        assertEquals(1, getEnqueueCount())

        clearWorkManager()

        observer.onStop(lifecycleOwner)
        ShadowSystemClock.advanceBy(Duration.ofMillis(1000)) // Less than 3000ms

        observer.onStart(lifecycleOwner)
        ShadowSystemClock.advanceBy(Duration.ofMillis(100))
        assertEquals(0, getEnqueueCount()) // Ensure no new enqueues
    }

    @Test
    fun `onStart triggers if debounce threshold is met`() = runTest {
        observer.onStart(lifecycleOwner)
        ShadowSystemClock.advanceBy(Duration.ofMillis(100))
        assertEquals(1, getEnqueueCount())

        clearWorkManager()

        observer.onStop(lifecycleOwner)
        ShadowSystemClock.advanceBy(Duration.ofMillis(3001))

        observer.onStart(lifecycleOwner)
        ShadowSystemClock.advanceBy(Duration.ofMillis(100))
        assertEquals(1, getEnqueueCount()) // Should re-enqueue
    }

    @Test
    fun `onStart skips if auth is blocked`() = runTest {
        syncOrchestrator.setAuthBlocked(true)
        observer.onStart(lifecycleOwner)
        ShadowSystemClock.advanceBy(Duration.ofMillis(100))

        assertEquals(0, getEnqueueCount())
    }

    @Test
    fun `overlapping execution skips via AtomicBoolean guard`() = runTest {
        // Manually force the guard to true
        observer.isRecoveryRunning.set(true)

        observer.onStart(lifecycleOwner)
        ShadowSystemClock.advanceBy(Duration.ofMillis(100))

        // Ensure no enqueue is called because the guard blocked it
        assertEquals(0, getEnqueueCount())
    }
}

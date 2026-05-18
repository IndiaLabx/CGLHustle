package com.cglhustle.core.sync.orchestrator

import android.os.SystemClock
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.mockito.Mockito.times
import org.mockito.Mockito.never
import org.mockito.kotlin.whenever
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowSystemClock
import java.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SyncLifecycleObserverTest {

    private val mockSyncOrchestrator: SyncOrchestrator = mock()
    private val mockLifecycleOwner: LifecycleOwner = mock()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var authBlockedFlow: MutableStateFlow<Boolean>
    private lateinit var observer: SyncLifecycleObserver

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        authBlockedFlow = MutableStateFlow(false)
        whenever(mockSyncOrchestrator.isAuthBlocked).thenReturn(authBlockedFlow)

        observer = SyncLifecycleObserver(mockSyncOrchestrator, testScope)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onStart triggers enqueueSync on cold start when auth is not blocked`() = testScope.runTest {
        authBlockedFlow.value = false

        observer.onStart(mockLifecycleOwner)
        advanceUntilIdle()

        verify(mockSyncOrchestrator, times(1)).enqueueSync()
    }

    @Test
    fun `onStart skips enqueueSync when auth is blocked`() = testScope.runTest {
        authBlockedFlow.value = true

        observer.onStart(mockLifecycleOwner)
        advanceUntilIdle()

        verify(mockSyncOrchestrator, never()).enqueueSync()
    }

    @Test
    fun `onStart skips enqueueSync when debounce threshold is not met`() = testScope.runTest {
        authBlockedFlow.value = false

        observer.onStop(mockLifecycleOwner)
        ShadowSystemClock.advanceBy(Duration.ofMillis(1000)) // Less than 3000ms

        observer.onStart(mockLifecycleOwner)
        advanceUntilIdle()

        verify(mockSyncOrchestrator, never()).enqueueSync()
    }

    @Test
    fun `onStart triggers enqueueSync when debounce threshold is met`() = testScope.runTest {
        authBlockedFlow.value = false

        observer.onStop(mockLifecycleOwner)
        ShadowSystemClock.advanceBy(Duration.ofMillis(3001)) // Greater than 3000ms

        observer.onStart(mockLifecycleOwner)
        advanceUntilIdle()

        verify(mockSyncOrchestrator, times(1)).enqueueSync()
    }

    @Test
    fun `onStart is guarded against overlapping executions`() = testScope.runTest {
        authBlockedFlow.value = false

        // Simulate a rapid double trigger
        observer.onStart(mockLifecycleOwner)
        observer.onStart(mockLifecycleOwner)

        advanceUntilIdle()

        // It should only run once despite two triggers
        verify(mockSyncOrchestrator, times(1)).enqueueSync()
    }
}

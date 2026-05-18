package com.cglhustle.core.sync.orchestrator

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SyncOrchestratorTest {

    private lateinit var context: Context
    private lateinit var workManager: WorkManager
    private lateinit var orchestrator: SyncOrchestrator

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        workManager = mock(WorkManager::class.java)
        orchestrator = SyncOrchestrator(context, workManager)
    }

    @Test
    fun `resumeSync clears AUTH_BLOCKED and enqueues sync`() = runTest {
        // First set to true to ensure it gets cleared
        orchestrator.setAuthBlocked(true)
        assertTrue(orchestrator.isAuthBlocked.first())

        orchestrator.resumeSync()

        assertFalse(orchestrator.isAuthBlocked.first())
        verify(workManager).enqueueUniqueWork(
            eq(SyncOrchestrator.UNIQUE_WORK_NAME),
            eq(ExistingWorkPolicy.KEEP),
            any(OneTimeWorkRequest::class.java)
        )
    }
}

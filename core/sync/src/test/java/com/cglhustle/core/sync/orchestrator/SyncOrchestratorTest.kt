package com.cglhustle.core.sync.orchestrator

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.cglhustle.core.common.logging.StructuredLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import java.io.File
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SyncOrchestratorTest {

    private lateinit var context: Context
    private lateinit var workManager: WorkManager
    private lateinit var syncOrchestrator: SyncOrchestrator
    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var mockLogger: StructuredLogger

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        workManager = mock(WorkManager::class.java)
        mockLogger = mock(StructuredLogger::class.java)

        testDataStore = PreferenceDataStoreFactory.create(
            produceFile = {
                File(context.filesDir, "datastore/test_sync_prefs.preferences_pb")
            }
        )

        syncOrchestrator = SyncOrchestrator(context, workManager, mockLogger)
    }

    @Test
    fun testEnqueueSync() {
        syncOrchestrator.enqueueSync()

        verify(workManager).enqueueUniqueWork(
            eq(SyncOrchestrator.UNIQUE_WORK_NAME),
            eq(ExistingWorkPolicy.KEEP),
            any<OneTimeWorkRequest>()
        )
    }

    @Test
    fun testResumeSync() = runTest {
        syncOrchestrator.resumeSync()

        verify(workManager).enqueueUniqueWork(
            eq(SyncOrchestrator.UNIQUE_WORK_NAME),
            eq(ExistingWorkPolicy.KEEP),
            any<OneTimeWorkRequest>()
        )
    }
}

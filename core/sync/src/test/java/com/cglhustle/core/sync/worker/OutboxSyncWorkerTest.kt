package com.cglhustle.core.sync.worker

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.cglhustle.core.database.dao.SyncEventDao
import com.cglhustle.core.database.entity.SyncEventEntity
import com.cglhustle.core.database.entity.SyncEventType
import com.cglhustle.core.database.entity.SyncStatus
import com.cglhustle.core.sync.network.FakeSyncNetworkDataSource
import com.cglhustle.core.sync.orchestrator.SyncOrchestrator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper

@RunWith(RobolectricTestRunner::class)
class OutboxSyncWorkerTest {

    private lateinit var context: Context
    private lateinit var syncEventDao: SyncEventDao
    private lateinit var fakeNetwork: FakeSyncNetworkDataSource
    private lateinit var syncOrchestrator: SyncOrchestrator
    private lateinit var workManager: WorkManager

    @androidx.room.Database(entities = [SyncEventEntity::class], version = 1, exportSchema = false)
    @androidx.room.TypeConverters(com.cglhustle.core.database.converter.RoomConverters::class)
    abstract class TestDatabase : androidx.room.RoomDatabase() {
        abstract fun syncEventDao(): SyncEventDao
    }

    private lateinit var testDatabase: TestDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        workManager = WorkManager.getInstance(context)

        testDatabase = Room.inMemoryDatabaseBuilder(
            context, TestDatabase::class.java
        ).allowMainThreadQueries().setTransactionExecutor(java.util.concurrent.Executors.newSingleThreadExecutor()).setQueryExecutor(java.util.concurrent.Executors.newSingleThreadExecutor()).build()
        syncEventDao = testDatabase.syncEventDao()

        fakeNetwork = FakeSyncNetworkDataSource()
        syncOrchestrator = SyncOrchestrator(context, workManager)

        runBlocking {
            syncOrchestrator.setAuthBlocked(false)
        }
    }

    @After
    fun teardown() {
        testDatabase.close()
    }

    @Test
    fun `Stale Recovery Test transitions old IN_FLIGHT to FAILED_RETRY`() = runTest {
        val now = System.currentTimeMillis()
        val staleTime = now - (20 * 60 * 1000L) // 20 minutes ago

        val staleEvent = SyncEventEntity(
            userId = "user1",
            idempotencyKey = "key1",
            eventType = SyncEventType.UPSERT_SESSION,
            payload = "{}",
            status = SyncStatus.IN_FLIGHT,
            createdAt = staleTime,
            nextRetryAt = null,
            retryCount = 0,
            lastErrorCode = null,
            lastErrorAt = null,
            lastAttemptAt = staleTime,
            processingToken = "old_token"
        )
        syncEventDao.insertEvent(staleEvent)

        fakeNetwork.shouldThrowTransientError = true

        val worker = TestListenableWorkerBuilder<OutboxSyncWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters
                ): ListenableWorker {
                    return OutboxSyncWorker(appContext, workerParameters, syncEventDao, fakeNetwork, syncOrchestrator)
                }
            })
            .build()

        worker.doWork()

        val events = syncEventDao.getPendingEvents(listOf(SyncStatus.FAILED_RETRY))
        assertEquals(1, events.size)
        assertEquals(SyncStatus.FAILED_RETRY, events[0].status)
    }

    @Test
    fun `Processing Lease Test ignores IN_FLIGHT rows with different token`() = runTest {
        val now = System.currentTimeMillis()
        val event = SyncEventEntity(
            userId = "user1", idempotencyKey = "key_lease", eventType = SyncEventType.UPSERT_SESSION,
            payload = "{}", status = SyncStatus.IN_FLIGHT, createdAt = now,
            nextRetryAt = null, retryCount = 0, lastErrorCode = null, lastErrorAt = null,
            lastAttemptAt = now,
            processingToken = "other_worker_token"
        )
        syncEventDao.insertEvent(event)

        val worker = TestListenableWorkerBuilder<OutboxSyncWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters
                ): ListenableWorker {
                    return OutboxSyncWorker(appContext, workerParameters, syncEventDao, fakeNetwork, syncOrchestrator)
                }
            })
            .build()

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)

        val pendingEvents = syncEventDao.countEventsWithStatus(listOf(SyncStatus.IN_FLIGHT))
        assertEquals(1, pendingEvents)
    }

    @Test
    fun `401 Batch Halt Test correctly halts and blocks auth`() = runTest {
        syncEventDao.insertEvent(SyncEventEntity(
            userId = "user1", idempotencyKey = "key1", eventType = SyncEventType.UPSERT_SESSION,
            payload = "{}", status = SyncStatus.PENDING, createdAt = System.currentTimeMillis(),
            nextRetryAt = null, retryCount = 0, lastErrorCode = null, lastErrorAt = null
        ))
        syncEventDao.insertEvent(SyncEventEntity(
            userId = "user1", idempotencyKey = "key2", eventType = SyncEventType.UPSERT_SESSION,
            payload = "{}", status = SyncStatus.PENDING, createdAt = System.currentTimeMillis(),
            nextRetryAt = null, retryCount = 0, lastErrorCode = null, lastErrorAt = null
        ))

        fakeNetwork.shouldThrowUnauthorized = true

        val worker = TestListenableWorkerBuilder<OutboxSyncWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters
                ): ListenableWorker {
                    return OutboxSyncWorker(appContext, workerParameters, syncEventDao, fakeNetwork, syncOrchestrator)
                }
            })
            .build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertTrue(syncOrchestrator.isAuthBlocked.first())

        val pendingEvents = syncEventDao.getPendingEvents(listOf(SyncStatus.PENDING))
        assertEquals(2, pendingEvents.size)
    }
}

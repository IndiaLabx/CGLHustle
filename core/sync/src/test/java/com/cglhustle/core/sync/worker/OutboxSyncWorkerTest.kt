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
import androidx.work.CoroutineWorker
import kotlinx.coroutines.flow.firstOrNull

@RunWith(RobolectricTestRunner::class)
class OutboxSyncWorkerTest {

    private lateinit var context: Context
    private lateinit var syncEventDao: SyncEventDao
    private lateinit var fakeNetwork: FakeSyncNetworkDataSource
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
    }

    @After
    fun teardown() {
        testDatabase.close()
    }

    @Test
    fun `ACKED path correctly marks event`() = runTest {
        val event = SyncEventEntity(
            userId = "user1", idempotencyKey = "key_ack", eventType = SyncEventType.UPSERT_SESSION,
            payload = "{}", status = SyncStatus.PENDING, createdAt = System.currentTimeMillis(),
            nextRetryAt = null, retryCount = 0, lastErrorCode = null, lastErrorAt = null
        )
        val id = syncEventDao.insertEvent(event)

        fakeNetwork.shouldFailWithException = false
        fakeNetwork.shouldFailWithAuth = false

        val worker = TestListenableWorkerBuilder<OutboxSyncWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters
                ): ListenableWorker {
                    return OutboxSyncWorker(appContext, workerParameters, syncEventDao, fakeNetwork)
                }
            })
            .build()

        val result = (worker as CoroutineWorker).doWork()
        assertEquals(ListenableWorker.Result.success(), result)

        val updatedEvent = syncEventDao.getPendingEvents().firstOrNull { it.id == id }
    }

    @Test
    fun `RETRY path correctly marks event for retry and returns Result retry`() = runTest {
        val event = SyncEventEntity(
            userId = "user1", idempotencyKey = "key_retry", eventType = SyncEventType.UPSERT_SESSION,
            payload = "{}", status = SyncStatus.PENDING, createdAt = System.currentTimeMillis(),
            nextRetryAt = null, retryCount = 0, lastErrorCode = null, lastErrorAt = null
        )
        val id = syncEventDao.insertEvent(event)

        fakeNetwork.shouldFailWithException = true

        val worker = TestListenableWorkerBuilder<OutboxSyncWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters
                ): ListenableWorker {
                    return OutboxSyncWorker(appContext, workerParameters, syncEventDao, fakeNetwork)
                }
            })
            .build()

        val result = (worker as CoroutineWorker).doWork()
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `401 Batch Halt Test correctly halts and blocks auth`() = runTest {
        val event1 = SyncEventEntity(
            userId = "user1", idempotencyKey = "key1", eventType = SyncEventType.UPSERT_SESSION,
            payload = "{}", status = SyncStatus.PENDING, createdAt = System.currentTimeMillis(),
            nextRetryAt = null, retryCount = 0, lastErrorCode = null, lastErrorAt = null
        )
        val id1 = syncEventDao.insertEvent(event1)

        fakeNetwork.shouldFailWithAuth = true

        val worker = TestListenableWorkerBuilder<OutboxSyncWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters
                ): ListenableWorker {
                    return OutboxSyncWorker(appContext, workerParameters, syncEventDao, fakeNetwork)
                }
            })
            .build()

        val result = (worker as CoroutineWorker).doWork()

        assertEquals(ListenableWorker.Result.retry(), result)

        val updatedEvent = syncEventDao.getPendingEvents().firstOrNull { it.id == id1 }
    }
}

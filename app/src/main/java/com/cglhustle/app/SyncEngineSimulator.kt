package com.cglhustle.app

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.cglhustle.core.database.dao.SyncEventDao
import com.cglhustle.core.database.entity.SyncEventEntity
import com.cglhustle.core.database.entity.SyncEventType
import com.cglhustle.core.database.entity.SyncStatus
import com.cglhustle.core.network.sync.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Simulator class to demonstrate the Offline-First and Multi-Device Conflict Resolution
 * requirements as outlined in the accepted contracts.
 */
class SyncEngineSimulator(
    private val context: Context,
    private val syncEventDao: SyncEventDao
) {
    fun runDryRunSimulation() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("SyncEngineSimulator", "--- Starting E2E Dry Run ---")

            // 1. User answers Question 1 on Device A (Offline)
            val answerPayload1 = """{"questionId":"q1", "selectedOption":"A", "attemptSequence":1}"""
            val event1 = SyncEventEntity(
                userId = "user_1",
                idempotencyKey = "user_1_session_1_q1_1_event1",
                eventType = SyncEventType.UPSERT_ANSWER,
                payload = answerPayload1,
                status = SyncStatus.PENDING,
                createdAt = System.currentTimeMillis(),
                nextRetryAt = null,
                retryCount = 0,
                lastErrorCode = null,
                lastErrorAt = null
            )
            syncEventDao.insertEvent(event1)
            Log.d("SyncEngineSimulator", "1. Device A: Answered Q1 -> Enqueued")

            // 2. User changes Answer on Device A (Offline) - Same attempt sequence, but newer event
            val answerPayload2 = """{"questionId":"q1", "selectedOption":"B", "attemptSequence":2}"""
            val event2 = SyncEventEntity(
                userId = "user_1",
                idempotencyKey = "user_1_session_1_q1_2_event2",
                eventType = SyncEventType.UPSERT_ANSWER,
                payload = answerPayload2,
                status = SyncStatus.PENDING,
                createdAt = System.currentTimeMillis() + 1000,
                nextRetryAt = null,
                retryCount = 0,
                lastErrorCode = null,
                lastErrorAt = null
            )
            syncEventDao.insertEvent(event2)
            Log.d("SyncEngineSimulator", "2. Device A: Changed Answer Q1 -> Enqueued")

            // 3. User finishes and submits the test
            val completePayload = """{"sessionId":"session_1", "status":"SUBMITTED_LOCAL"}"""
            val completeEvent = SyncEventEntity(
                userId = "user_1",
                idempotencyKey = "user_1_session_1_complete",
                eventType = SyncEventType.MARK_COMPLETED,
                payload = completePayload,
                status = SyncStatus.PENDING,
                createdAt = System.currentTimeMillis() + 2000,
                nextRetryAt = null,
                retryCount = 0,
                lastErrorCode = null,
                lastErrorAt = null
            )
            syncEventDao.insertEvent(completeEvent)
            Log.d("SyncEngineSimulator", "3. Device A: Submitted Test -> Enqueued")

            // 4. Trigger SyncWorker
            Log.d("SyncEngineSimulator", "4. Network restored. Triggering SyncWorker...")
            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "sync_work",
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )

            // Allow some time for the worker to process (in a real scenario we'd observe WorkInfo)
            kotlinx.coroutines.delay(2000)

            // 5. Verify the Outbox is cleared/acked
            val pending = syncEventDao.getPendingEvents()
            Log.d("SyncEngineSimulator", "5. Sync complete. Pending events left: ${pending.size}")

            if (pending.isEmpty()) {
                Log.d("SyncEngineSimulator", "--- E2E Dry Run Success! ---")
            } else {
                Log.e("SyncEngineSimulator", "--- E2E Dry Run Failed: events still pending ---")
            }
        }
    }
}

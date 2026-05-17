package com.cglhustle.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.cglhustle.core.database.entity.SyncEventEntity
import com.cglhustle.core.database.entity.UserAnswerEntity

@Dao
abstract class UserAnswerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertAnswerInternal(answer: UserAnswerEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertSyncEventInternal(event: SyncEventEntity): Long

    @Query("UPDATE quiz_sessions SET sessionVersion = sessionVersion + 1, lastMutationId = :lastMutationId, updatedAt = :updatedAt WHERE sessionId = :sessionId")
    protected abstract suspend fun updateQuizSessionVersionInternal(sessionId: String, lastMutationId: String, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM sync_events WHERE userId = :userId AND idempotencyKey = :idempotencyKey")
    protected abstract suspend fun checkEventExists(userId: String, idempotencyKey: String): Int

    @Transaction
    open suspend fun saveAnswerWithOutbox(
        answer: UserAnswerEntity,
        syncEvent: SyncEventEntity,
        timestamp: Long
    ) {
        // 1. Check local idempotency for the sync event
        val exists = checkEventExists(syncEvent.userId, syncEvent.idempotencyKey)

        // 2. Persist/Update the answer
        insertAnswerInternal(answer)

        // 3. Update the quiz session's version and last mutation ID
        updateQuizSessionVersionInternal(answer.sessionId, answer.eventId, timestamp)

        // 4. Enqueue the sync event if it doesn't exist
        if (exists == 0) {
            insertSyncEventInternal(syncEvent)
        }
    }

    @Query("SELECT * FROM user_answers WHERE sessionId = :sessionId AND questionId = :questionId ORDER BY attemptSequence DESC LIMIT 1")
    abstract suspend fun getLatestAnswerForQuestion(sessionId: String, questionId: String): UserAnswerEntity?

    @Query("SELECT * FROM user_answers WHERE sessionId = :sessionId ORDER BY attemptSequence ASC")
    abstract suspend fun getAllAnswersForSession(sessionId: String): List<UserAnswerEntity>
}

package com.cglhustle.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.cglhustle.core.database.entity.SyncEventEntity
import com.cglhustle.core.database.entity.UserAnswerEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserAnswerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAnswer(answer: UserAnswerEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertSyncEvent(event: SyncEventEntity): Long

    @Query("SELECT COUNT(*) FROM sync_events WHERE userId = :userId AND idempotencyKey = :idempotencyKey")
    abstract suspend fun checkEventExists(userId: String, idempotencyKey: String): Int

    @Query("SELECT * FROM user_answers WHERE sessionId = :sessionId AND questionId = :questionId ORDER BY attemptSequence DESC LIMIT 1")
    abstract suspend fun getLatestAnswerForQuestion(sessionId: String, questionId: String): UserAnswerEntity?

    @Query("SELECT * FROM user_answers WHERE sessionId = :sessionId ORDER BY attemptSequence ASC")
    abstract suspend fun getAllAnswersForSession(sessionId: String): List<UserAnswerEntity>

    @Query("SELECT * FROM user_answers WHERE sessionId = :sessionId ORDER BY attemptSequence ASC")
    abstract fun observeAllAnswersForSession(sessionId: String): Flow<List<UserAnswerEntity>>
}

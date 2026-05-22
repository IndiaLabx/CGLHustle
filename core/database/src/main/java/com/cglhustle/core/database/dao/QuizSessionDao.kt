package com.cglhustle.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cglhustle.core.database.entity.QuizSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuizSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSession(session: QuizSessionEntity)

    @Update
    abstract suspend fun updateSession(session: QuizSessionEntity)

    @Query("SELECT * FROM quiz_sessions WHERE sessionId = :sessionId")
    abstract suspend fun getSessionById(sessionId: String): QuizSessionEntity?

    @Query("SELECT * FROM quiz_sessions WHERE userId = :userId")
    abstract suspend fun getSessionsByUser(userId: String): List<QuizSessionEntity>

    @Query("SELECT * FROM quiz_sessions WHERE sessionId = :sessionId")
    abstract fun observeSessionById(sessionId: String): Flow<QuizSessionEntity?>

    @Query("UPDATE quiz_sessions SET status = :status, lastMutationId = :lastMutationId, updatedAt = :updatedAt WHERE sessionId = :sessionId")
    abstract suspend fun updateSessionStatus(sessionId: String, status: String, lastMutationId: String, updatedAt: Long)

    @Query("UPDATE quiz_sessions SET status = :status, lastMutationId = :lastMutationId, updatedAt = :updatedAt, totalPausedDurationMs = :totalPausedDurationMs, lastPausedTime = :lastPausedTime WHERE sessionId = :sessionId")
    abstract suspend fun updateSessionPauseState(sessionId: String, status: String, lastMutationId: String, updatedAt: Long, totalPausedDurationMs: Long, lastPausedTime: Long?)

    @Query("UPDATE quiz_sessions SET sessionVersion = sessionVersion + 1, lastMutationId = :lastMutationId, updatedAt = :updatedAt WHERE sessionId = :sessionId")
    abstract suspend fun updateSessionVersion(sessionId: String, lastMutationId: String, updatedAt: Long)
}

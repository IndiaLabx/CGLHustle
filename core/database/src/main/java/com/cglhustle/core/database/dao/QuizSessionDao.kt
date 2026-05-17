package com.cglhustle.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cglhustle.core.database.entity.QuizSessionEntity

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
}

package com.cglhustle.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cglhustle.core.database.entity.QuizSessionEntity
import java.util.UUID

@Dao
interface QuizSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: QuizSessionEntity)

    @Update
    suspend fun updateSession(session: QuizSessionEntity)

    @Query("SELECT * FROM quiz_sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: UUID): QuizSessionEntity?

    @Query("SELECT * FROM quiz_sessions WHERE userId = :userId")
    suspend fun getSessionsByUser(userId: UUID): List<QuizSessionEntity>
}

package com.cglhustle.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cglhustle.core.database.entity.UserAnswerEntity
import java.util.UUID

@Dao
interface UserAnswerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: UserAnswerEntity)

    @Query("SELECT * FROM user_answers WHERE sessionId = :sessionId AND questionId = :questionId ORDER BY attemptSequence DESC LIMIT 1")
    suspend fun getLatestAnswerForQuestion(sessionId: UUID, questionId: UUID): UserAnswerEntity?

    @Query("SELECT * FROM user_answers WHERE sessionId = :sessionId ORDER BY attemptSequence ASC")
    suspend fun getAllAnswersForSession(sessionId: UUID): List<UserAnswerEntity>
}

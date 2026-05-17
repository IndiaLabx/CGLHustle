package com.cglhustle.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cglhustle.core.database.entity.QuestionSnapshotEntity

@Dao
abstract class QuestionSnapshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSnapshots(snapshots: List<QuestionSnapshotEntity>)

    @Query("SELECT * FROM question_snapshots WHERE quizSessionId = :quizSessionId")
    abstract suspend fun getSnapshotsForSession(quizSessionId: String): List<QuestionSnapshotEntity>
}

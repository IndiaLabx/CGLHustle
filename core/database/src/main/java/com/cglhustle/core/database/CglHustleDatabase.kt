package com.cglhustle.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cglhustle.core.database.entity.*
import com.cglhustle.core.database.dao.*
import com.cglhustle.core.database.converter.RoomConverters

@Database(
    entities = [
        QuestionSnapshotEntity::class,
        QuizSessionEntity::class,
        UserAnswerEntity::class,
        SyncEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class CglHustleDatabase : RoomDatabase() {
    abstract fun syncEventDao(): SyncEventDao
    abstract fun quizSessionDao(): QuizSessionDao
    abstract fun userAnswerDao(): UserAnswerDao
    abstract fun questionSnapshotDao(): QuestionSnapshotDao
}

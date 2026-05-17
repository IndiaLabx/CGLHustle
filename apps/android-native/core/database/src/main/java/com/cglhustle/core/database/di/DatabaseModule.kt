package com.cglhustle.core.database.di

import android.content.Context
import androidx.room.Room
import com.cglhustle.core.database.CglHustleDatabase
import com.cglhustle.core.database.dao.QuizSessionDao
import com.cglhustle.core.database.dao.SyncEventDao
import com.cglhustle.core.database.dao.UserAnswerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CglHustleDatabase {
        return Room.databaseBuilder(
            context,
            CglHustleDatabase::class.java,
            "cgl_hustle_db"
        ).build()
    }

    @Provides
    fun provideSyncEventDao(db: CglHustleDatabase): SyncEventDao = db.syncEventDao()

    @Provides
    fun provideQuizSessionDao(db: CglHustleDatabase): QuizSessionDao = db.quizSessionDao()

    @Provides
    fun provideUserAnswerDao(db: CglHustleDatabase): UserAnswerDao = db.userAnswerDao()
}

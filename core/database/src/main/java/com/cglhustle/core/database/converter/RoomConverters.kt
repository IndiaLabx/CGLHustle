package com.cglhustle.core.database.converter

import androidx.room.TypeConverter
import com.cglhustle.core.database.entity.AnswerMutationType
import com.cglhustle.core.database.entity.SessionStatus
import com.cglhustle.core.database.entity.SyncEventType
import com.cglhustle.core.database.entity.SyncStatus

class RoomConverters {

    @TypeConverter
    fun fromSessionStatus(status: SessionStatus): String = status.name

    @TypeConverter
    fun toSessionStatus(name: String): SessionStatus = SessionStatus.valueOf(name)

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.name

    @TypeConverter
    fun toSyncStatus(name: String): SyncStatus = SyncStatus.valueOf(name)

    @TypeConverter
    fun fromAnswerMutationType(type: AnswerMutationType): String = type.name

    @TypeConverter
    fun toAnswerMutationType(name: String): AnswerMutationType = AnswerMutationType.valueOf(name)

    @TypeConverter
    fun fromSyncEventType(type: SyncEventType): String = type.name

    @TypeConverter
    fun toSyncEventType(name: String): SyncEventType = SyncEventType.valueOf(name)

}

package com.cglhustle.core.database.converter

import androidx.room.TypeConverter
import com.cglhustle.core.database.entity.AnswerMutationType
import com.cglhustle.core.database.entity.SessionStatus
import com.cglhustle.core.database.entity.SyncEventType
import com.cglhustle.core.database.entity.SyncStatus
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

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

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        if (list == null) return null
        val jsonArray = JSONArray()
        for (item in list) {
            jsonArray.put(item)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toStringList(jsonString: String?): List<String>? {
        if (jsonString == null) return null
        try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            return list
        } catch (e: JSONException) {
            return emptyList()
        }
    }

    @TypeConverter
    fun fromStringMap(map: Map<String, String>?): String? {
        if (map == null) return null
        val jsonObject = JSONObject()
        for ((key, value) in map) {
            jsonObject.put(key, value)
        }
        return jsonObject.toString()
    }

    @TypeConverter
    fun toStringMap(jsonString: String?): Map<String, String>? {
        if (jsonString == null) return null
        try {
            val jsonObject = JSONObject(jsonString)
            val map = mutableMapOf<String, String>()
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = jsonObject.getString(key)
            }
            return map
        } catch (e: JSONException) {
            return emptyMap()
        }
    }
}

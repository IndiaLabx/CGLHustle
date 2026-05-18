package com.cglhustle.core.database.converter

import com.cglhustle.core.database.entity.AnswerMutationType
import com.cglhustle.core.database.entity.SessionStatus
import com.cglhustle.core.database.entity.SyncEventType
import com.cglhustle.core.database.entity.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomConvertersTest {
    private val converters = RoomConverters()

    @Test
    fun testSessionStatusRoundTrip() {
        val original = SessionStatus.IN_PROGRESS
        val asString = converters.fromSessionStatus(original)
        assertEquals("IN_PROGRESS", asString)
        val backToEnum = converters.toSessionStatus(asString)
        assertEquals(original, backToEnum)
    }

    @Test
    fun testSyncStatusRoundTrip() {
        val original = SyncStatus.PENDING
        val asString = converters.fromSyncStatus(original)
        assertEquals("PENDING", asString)
        val backToEnum = converters.toSyncStatus(asString)
        assertEquals(original, backToEnum)
    }

    @Test
    fun testAnswerMutationTypeRoundTrip() {
        val original = AnswerMutationType.SELECT
        val asString = converters.fromAnswerMutationType(original)
        assertEquals("SELECT", asString)
        val backToEnum = converters.toAnswerMutationType(asString)
        assertEquals(original, backToEnum)
    }

    @Test
    fun testSyncEventTypeRoundTrip() {
        val original = SyncEventType.UPSERT_ANSWER
        val asString = converters.fromSyncEventType(original)
        assertEquals("UPSERT_ANSWER", asString)
        val backToEnum = converters.toSyncEventType(asString)
        assertEquals(original, backToEnum)
    }

    @Test
    fun testStringListRoundTrip() {
        val list = listOf("A", "B", "C")
        val jsonString = converters.fromStringList(list)
        val backToList = converters.toStringList(jsonString)
        assertEquals(list, backToList)

        val emptyJson = converters.fromStringList(emptyList())
        val backToEmpty = converters.toStringList(emptyJson)
        assertEquals(emptyList<String>(), backToEmpty)
    }

    @Test
    fun testStringMapRoundTrip() {
        val map = mapOf("en" to "Explanation", "hi" to "व्याख्या")
        val jsonString = converters.fromStringMap(map)
        val backToMap = converters.toStringMap(jsonString)
        assertEquals(map, backToMap)

        val emptyJson = converters.fromStringMap(emptyMap())
        val backToEmpty = converters.toStringMap(emptyJson)
        assertEquals(emptyMap<String, String>(), backToEmpty)
    }
}

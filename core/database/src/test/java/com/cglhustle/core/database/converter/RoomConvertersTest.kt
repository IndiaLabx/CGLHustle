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
}

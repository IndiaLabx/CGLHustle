package com.cglhustle.engine.facetedsearch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class QuizSearchEngineTest {

    private lateinit var engine: QuizSearchEngine

    @Before
    fun setup() {
        engine = QuizSearchEngine()

        val metadataList = listOf(
            EngineMetadata("id1", "History", "Ancient", "Rome", "Hard", "MCQ", "ExamA", "2020", listOf("tag1")),
            EngineMetadata("id2", "History", "Ancient", "Greece", "Easy", "MCQ", "ExamA", "2020", listOf("tag1", "tag2")),
            EngineMetadata("id3", "Geography", "Maps", "Europe", "Hard", "FIB", "ExamB", "2021", emptyList()),
            EngineMetadata("id4", "History", "Modern", "WW2", "Hard", "MCQ", "ExamA", "2022", listOf("tag3"))
        )

        engine.buildIndex(metadataList)
    }

    @Test
    fun `test empty query returns all documents`() {
        val result = engine.query(FilterQuery())
        assertEquals(4, result.totalMatches)
    }

    @Test
    fun `test single category selection (OR logic)`() {
        val query = FilterQuery(
            selections = mapOf(
                FilterCategory.SUBJECT to setOf("History")
            )
        )

        val result = engine.query(query)
        assertEquals(3, result.totalMatches)

        // Subject count should reflect full universe (since we exclude SUBJECT when calculating SUBJECT counts)
        // History is 3, Geography is 1
        assertEquals(3, result.facetCounts[FilterCategory.SUBJECT]?.get("History"))
        assertEquals(1, result.facetCounts[FilterCategory.SUBJECT]?.get("Geography"))
    }

    @Test
    fun `test multi category selection (AND logic)`() {
        val query = FilterQuery(
            selections = mapOf(
                FilterCategory.SUBJECT to setOf("History"),
                FilterCategory.DIFFICULTY to setOf("Hard")
            )
        )

        val result = engine.query(query)
        assertEquals(2, result.totalMatches) // id1, id4
    }

    @Test
    fun `test hierarchy rules enforcement`() {
        // No subject selected
        var query = FilterQuery()
        var result = engine.query(query)
        assertTrue(result.visibleValues[FilterCategory.TOPIC].isNullOrEmpty())
        assertTrue(result.visibleValues[FilterCategory.SUB_TOPIC].isNullOrEmpty())

        // Subject selected, but no topic
        query = FilterQuery(
            selections = mapOf(FilterCategory.SUBJECT to setOf("History"))
        )
        result = engine.query(query)
        assertTrue(!result.visibleValues[FilterCategory.TOPIC].isNullOrEmpty())
        assertTrue(result.visibleValues[FilterCategory.SUB_TOPIC].isNullOrEmpty())

        // Subject and Topic selected
        query = FilterQuery(
            selections = mapOf(
                FilterCategory.SUBJECT to setOf("History"),
                FilterCategory.TOPIC to setOf("Ancient")
            )
        )
        result = engine.query(query)
        assertTrue(!result.visibleValues[FilterCategory.TOPIC].isNullOrEmpty())
        assertTrue(!result.visibleValues[FilterCategory.SUB_TOPIC].isNullOrEmpty())
    }

    @Test
    fun `test id extraction only on demand`() {
        val query = FilterQuery(
            selections = mapOf(
                FilterCategory.SUBJECT to setOf("History"),
                FilterCategory.DIFFICULTY to setOf("Hard")
            )
        )

        val ids = engine.extractFinalIds(query, maxLimit = 10)
        assertEquals(2, ids.size)
        assertTrue(ids.contains("id1"))
        assertTrue(ids.contains("id4"))
    }
}

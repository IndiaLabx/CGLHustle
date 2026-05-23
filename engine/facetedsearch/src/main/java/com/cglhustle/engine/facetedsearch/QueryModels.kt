package com.cglhustle.engine.facetedsearch

enum class FilterCategory {
    SUBJECT,
    TOPIC,
    SUB_TOPIC,
    DIFFICULTY,
    EXAM_NAME,
    EXAM_YEAR,
    SHIFT,
    TAGS
}

data class FilterQuery(
    val selections: Map<FilterCategory, Set<String>> = emptyMap()
)

data class QueryResult(
    val totalMatches: Int,
    val facetCounts: Map<FilterCategory, Map<String, Int>>,
    val visibleValues: Map<FilterCategory, Set<String>>
)

data class EngineMetadata(
    val id: String,
    val subject: String,
    val topic: String,
    val subTopic: String,
    val difficulty: String,
    val questionType: String,
    val examName: String,
    val examYear: String,
    val tags: List<String>
)

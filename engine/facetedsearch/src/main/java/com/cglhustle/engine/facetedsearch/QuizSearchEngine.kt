package com.cglhustle.engine.facetedsearch

import java.util.BitSet
import java.util.concurrent.atomic.AtomicBoolean

class QuizSearchEngine {

    // UUID Mapping
    private val intToId = mutableListOf<String>()
    private val idToInt = mutableMapOf<String, Int>()

    // Index: Category -> Value -> BitSet
    private val index: MutableMap<FilterCategory, MutableMap<String, BitSet>> = mutableMapOf()

    // Valid unique values observed for each category during ingestion
    private val allObservedValues: MutableMap<FilterCategory, MutableSet<String>> = mutableMapOf()

    private var totalDocuments = 0
    private val isIndexed = AtomicBoolean(false)

    // Query-local scratchpads (avoid instantiation during query execution)
    private val universeScratch = BitSet()
    private val categoryScratch = BitSet()
    private val tempCountScratch = BitSet()

    // Universe (All bits 1 up to totalDocuments)
    private val fullUniverse = BitSet()

    fun buildIndex(metadataList: List<EngineMetadata>) {
        // Reset everything
        intToId.clear()
        idToInt.clear()
        index.clear()
        allObservedValues.clear()
        totalDocuments = metadataList.size

        fullUniverse.clear()
        fullUniverse.set(0, totalDocuments)

        FilterCategory.entries.forEach { category ->
            index[category] = mutableMapOf()
            allObservedValues[category] = mutableSetOf()
        }

        // Single pass ingestion
        metadataList.forEachIndexed { docId, metadata ->
            intToId.add(metadata.id)
            idToInt[metadata.id] = docId

            addValueToIndex(FilterCategory.SUBJECT, metadata.subject, docId)
            addValueToIndex(FilterCategory.TOPIC, metadata.topic, docId)
            addValueToIndex(FilterCategory.SUB_TOPIC, metadata.subTopic, docId)
            addValueToIndex(FilterCategory.DIFFICULTY, metadata.difficulty, docId)
            addValueToIndex(FilterCategory.EXAM_NAME, metadata.examName, docId)
            addValueToIndex(FilterCategory.EXAM_YEAR, metadata.examYear, docId)
            addValueToIndex(FilterCategory.SHIFT, "", docId) // Assuming shift isn't on EngineMetadata currently, stubbed.

            metadata.tags.forEach { tag ->
                addValueToIndex(FilterCategory.TAGS, tag, docId)
            }
        }

        isIndexed.set(true)
    }

    private fun addValueToIndex(category: FilterCategory, value: String, docId: Int) {
        if (value.isBlank()) return

        allObservedValues[category]?.add(value)

        val categoryIndex = index[category]!!
        val bitSet = categoryIndex.getOrPut(value) { BitSet() }
        bitSet.set(docId)
    }

    /**
     * Executes a query efficiently utilizing zero-allocation reusable BitSets.
     * Only extracts numerical counts and visibility state, NOT the UUIDs.
     */
    fun query(query: FilterQuery): QueryResult {
        if (!isIndexed.get() || totalDocuments == 0) {
            return QueryResult(0, emptyMap(), emptyMap())
        }

        // 1. Calculate Active Universe base (intersection of all selected categories)
        universeScratch.clear()
        universeScratch.or(fullUniverse)

        FilterCategory.entries.forEach { category ->
            val selections = query.selections[category]
            if (selections != null && selections.isNotEmpty()) {
                categoryScratch.clear()

                // OR logic within same category
                selections.forEach { value ->
                    val bitsForValue = index[category]?.get(value)
                    if (bitsForValue != null) {
                        categoryScratch.or(bitsForValue)
                    }
                }

                // AND logic across categories
                universeScratch.and(categoryScratch)
            }
        }

        val totalMatches = universeScratch.cardinality()

        // 2. Compute dynamic counts & visibility per category
        val facetCounts = mutableMapOf<FilterCategory, Map<String, Int>>()
        val visibleValues = mutableMapOf<FilterCategory, MutableSet<String>>()

        FilterCategory.entries.forEach { category ->
            val categoryCounts = mutableMapOf<String, Int>()
            val visibleSet = mutableSetOf<String>()

            // To find dynamic count for value X in Category C:
            // We need to intersect X with all OTHER active filters, excluding C.
            // Re-compute active universe excluding current category
            tempCountScratch.clear()
            tempCountScratch.or(fullUniverse)

            FilterCategory.entries.forEach { otherCategory ->
                if (otherCategory != category) {
                    val otherSelections = query.selections[otherCategory]
                    if (otherSelections != null && otherSelections.isNotEmpty()) {
                        categoryScratch.clear()
                        otherSelections.forEach { value ->
                            val bitsForValue = index[otherCategory]?.get(value)
                            if (bitsForValue != null) {
                                categoryScratch.or(bitsForValue)
                            }
                        }
                        tempCountScratch.and(categoryScratch)
                    }
                }
            }

            // Iterate over all valid values for this category to determine if they're visible and their count
            allObservedValues[category]?.forEach { value ->
                val bitsForValue = index[category]?.get(value)
                if (bitsForValue != null) {

                    // We only want to compute count if tempCountScratch (other filters) intersects with this value
                    val isPotentiallyVisible = tempCountScratch.intersects(bitsForValue)

                    if (isPotentiallyVisible) {
                        // We use a clone here just to get the cardinality easily without mutating tempCountScratch
                        // Since we just check intersections and counts.
                        // Optimization: BitSet doesn't have an `andCardinality` method natively.
                        // To achieve zero-allocation, we can temporarily AND it, count, then XOR it back, or just use another scratch.
                        categoryScratch.clear()
                        categoryScratch.or(tempCountScratch)
                        categoryScratch.and(bitsForValue)

                        val count = categoryScratch.cardinality()
                        if (count > 0) {
                            categoryCounts[value] = count
                            visibleSet.add(value)
                        }
                    }
                }
            }

            facetCounts[category] = categoryCounts
            visibleValues[category] = visibleSet
        }

        // Enforce Hierarchy Rules (Subject -> Topic -> SubTopic)
        enforceHierarchyVisibility(query, visibleValues)

        return QueryResult(
            totalMatches = totalMatches,
            facetCounts = facetCounts,
            visibleValues = visibleValues
        )
    }

    private fun enforceHierarchyVisibility(
        query: FilterQuery,
        visibleValues: MutableMap<FilterCategory, MutableSet<String>>
    ) {
        val selectedSubjects = query.selections[FilterCategory.SUBJECT]
        val selectedTopics = query.selections[FilterCategory.TOPIC]

        // Rule: Topics hidden until Subject selected
        if (selectedSubjects.isNullOrEmpty()) {
            visibleValues[FilterCategory.TOPIC] = mutableSetOf()
            visibleValues[FilterCategory.SUB_TOPIC] = mutableSetOf()
        }
        // Rule: Subtopics hidden until Topic selected
        else if (selectedTopics.isNullOrEmpty()) {
            visibleValues[FilterCategory.SUB_TOPIC] = mutableSetOf()
        }
    }

    /**
     * Expensive operation: Only run when user explicitly clicks "Create Quiz".
     * Extracts physical String UUIDs.
     */
    fun extractFinalIds(query: FilterQuery, maxLimit: Int): List<String> {
        if (!isIndexed.get() || totalDocuments == 0) return emptyList()

        // 1. Calculate Active Universe
        universeScratch.clear()
        universeScratch.or(fullUniverse)

        FilterCategory.entries.forEach { category ->
            val selections = query.selections[category]
            if (selections != null && selections.isNotEmpty()) {
                categoryScratch.clear()

                selections.forEach { value ->
                    val bitsForValue = index[category]?.get(value)
                    if (bitsForValue != null) {
                        categoryScratch.or(bitsForValue)
                    }
                }
                universeScratch.and(categoryScratch)
            }
        }

        val resultIds = mutableListOf<String>()
        var bitIndex = universeScratch.nextSetBit(0)

        while (bitIndex != -1 && resultIds.size < maxLimit) {
            resultIds.add(intToId[bitIndex])
            bitIndex = universeScratch.nextSetBit(bitIndex + 1)
        }

        return resultIds
    }
}

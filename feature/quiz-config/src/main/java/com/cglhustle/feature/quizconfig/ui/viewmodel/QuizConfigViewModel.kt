package com.cglhustle.feature.quizconfig.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cglhustle.feature.quizconfig.domain.model.QuestionMetadata
import com.cglhustle.feature.quizconfig.domain.model.QuizFilterOptions
import com.cglhustle.feature.quizconfig.domain.model.QuizMode
import com.cglhustle.feature.quizconfig.domain.repository.QuestionMetadataRepository
import com.cglhustle.feature.quizconfig.domain.repository.QuizRepository
import com.cglhustle.feature.quizconfig.ui.state.FilterType
import com.cglhustle.feature.quizconfig.ui.state.QuizConfigUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class QuizConfigViewModel @Inject constructor(
    private val metadataRepository: QuestionMetadataRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizConfigUiState())
    val uiState: StateFlow<QuizConfigUiState> = _uiState.asStateFlow()

    private var allMetadata: List<QuestionMetadata> = emptyList()

    // Category -> Value -> Set of Question IDs
    private var invertedIndex: Map<FilterType, Map<String, Set<String>>> = emptyMap()

    private var currentFilteredIds: Set<String> = emptySet()

    init {
        loadMetadata()
    }

    fun loadMetadata() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFilters = true, error = null) }
            try {
                allMetadata = metadataRepository.fetchMetadata()
                buildInvertedIndex(allMetadata)

                val subjects = allMetadata.map { it.subject }.filter { it.isNotEmpty() }.distinct().sorted()
                val topics = allMetadata.map { it.topic }.filter { it.isNotEmpty() }.distinct().sorted()
                val subTopics = allMetadata.map { it.subTopic }.filter { it.isNotEmpty() }.distinct().sorted()
                val difficulties = allMetadata.map { it.difficulty }.filter { it.isNotEmpty() }.distinct().sorted()
                val examNames = allMetadata.map { it.examName }.filter { it.isNotEmpty() }.distinct().sorted()
                val examYears = allMetadata.map { it.examYear }.filter { it.isNotEmpty() }.distinct().sorted()
                val tags = allMetadata.flatMap { it.tags }.filter { it.isNotEmpty() }.distinct().sorted()

                val options = QuizFilterOptions(
                    subjects = subjects,
                    topics = topics,
                    subTopics = subTopics,
                    difficulties = difficulties,
                    examNames = examNames,
                    examYears = examYears,
                    shifts = emptyList(),
                    tags = tags
                )

                updateFilteredCounts()

                _uiState.update {
                    it.copy(
                        isLoadingFilters = false,
                        filterOptions = options
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingFilters = false,
                        error = e.localizedMessage ?: "Failed to load metadata"
                    )
                }
            }
        }
    }

    private suspend fun buildInvertedIndex(metadata: List<QuestionMetadata>) = withContext(Dispatchers.Default) {
        val index = mutableMapOf<FilterType, MutableMap<String, MutableSet<String>>>()
        FilterType.entries.forEach { index[it] = mutableMapOf() }

        metadata.forEach { q ->
            if (q.subject.isNotEmpty()) index[FilterType.SUBJECT]!!.getOrPut(q.subject) { mutableSetOf() }.add(q.id)
            if (q.topic.isNotEmpty()) index[FilterType.TOPIC]!!.getOrPut(q.topic) { mutableSetOf() }.add(q.id)
            if (q.subTopic.isNotEmpty()) index[FilterType.SUB_TOPIC]!!.getOrPut(q.subTopic) { mutableSetOf() }.add(q.id)
            if (q.difficulty.isNotEmpty()) index[FilterType.DIFFICULTY]!!.getOrPut(q.difficulty) { mutableSetOf() }.add(q.id)
            if (q.examName.isNotEmpty()) index[FilterType.EXAM_NAME]!!.getOrPut(q.examName) { mutableSetOf() }.add(q.id)
            if (q.examYear.isNotEmpty()) index[FilterType.EXAM_YEAR]!!.getOrPut(q.examYear) { mutableSetOf() }.add(q.id)
            q.tags.forEach { tag ->
                if (tag.isNotEmpty()) index[FilterType.TAGS]!!.getOrPut(tag) { mutableSetOf() }.add(q.id)
            }
        }
        invertedIndex = index
    }

    private fun getIntersectionExcluding(excludedType: FilterType?): Set<String> {
        val state = _uiState.value
        var currentSet: Set<String>? = null

        fun intersectWith(type: FilterType, values: Set<String>) {
            if (type == excludedType || values.isEmpty()) return
            val typeMap = invertedIndex[type] ?: return

            val valuesUnion = mutableSetOf<String>()
            values.forEach { v ->
                typeMap[v]?.let { valuesUnion.addAll(it) }
            }

            currentSet = if (currentSet == null) {
                valuesUnion
            } else {
                currentSet!!.intersect(valuesUnion)
            }
        }

        intersectWith(FilterType.SUBJECT, state.selectedSubjects)
        intersectWith(FilterType.TOPIC, state.selectedTopics)
        intersectWith(FilterType.SUB_TOPIC, state.selectedSubTopics)
        intersectWith(FilterType.DIFFICULTY, state.selectedDifficulties)
        intersectWith(FilterType.EXAM_NAME, state.selectedExamNames)
        intersectWith(FilterType.EXAM_YEAR, state.selectedYears)
        intersectWith(FilterType.SHIFT, state.selectedShifts)
        intersectWith(FilterType.TAGS, state.selectedTags)

        return currentSet ?: allMetadata.map { it.id }.toSet()
    }

    private fun updateFilteredCounts() {
        val globalIds = getIntersectionExcluding(null)
        currentFilteredIds = globalIds

        val dynamicCounts = mutableMapOf<FilterType, Map<String, Int>>()

        // Calculate dynamic counts for EACH filter category independently
        FilterType.entries.forEach { type ->
            val setExcludingCurrentType = getIntersectionExcluding(type)
            val countsForType = mutableMapOf<String, Int>()

            invertedIndex[type]?.forEach { (value, ids) ->
                // How many items match if we add THIS value to ALL OTHER selected filters?
                countsForType[value] = setExcludingCurrentType.intersect(ids).size
            }
            dynamicCounts[type] = countsForType
        }

        _uiState.update {
            it.copy(
                availableQuestionCount = currentFilteredIds.size,
                dynamicCounts = dynamicCounts
            )
        }

        verifyDependencies()
    }

    private fun verifyDependencies() {
        val state = _uiState.value

        // Find valid options based on dynamic counts (anything > 0 is valid)
        val validTopics = state.dynamicCounts[FilterType.TOPIC]?.filterValues { it > 0 }?.keys ?: emptySet()
        val validSubTopics = state.dynamicCounts[FilterType.SUB_TOPIC]?.filterValues { it > 0 }?.keys ?: emptySet()

        val newTopics = state.selectedTopics.intersect(validTopics)
        val newSubTopics = state.selectedSubTopics.intersect(validSubTopics)

        if (newTopics != state.selectedTopics || newSubTopics != state.selectedSubTopics) {
            _uiState.update {
                it.copy(
                    selectedTopics = newTopics,
                    selectedSubTopics = newSubTopics
                )
            }
            // Need to recurse lightly to ensure counts are updated post-pruning
            // We use a separate minimal pass so we don't endless loop
            val globalIds = getIntersectionExcluding(null)
            currentFilteredIds = globalIds
            _uiState.update { it.copy(availableQuestionCount = currentFilteredIds.size) }
        }
    }

    // --- Intent Handlers ---

    private fun toggleSet(current: Set<String>, value: String): Set<String> {
        return if (current.contains(value)) current - value else current + value
    }

    fun selectSubject(subject: String) {
        _uiState.update { it.copy(selectedSubjects = toggleSet(it.selectedSubjects, subject)) }
        updateFilteredCounts()
    }

    fun selectTopic(topic: String) {
        _uiState.update { it.copy(selectedTopics = toggleSet(it.selectedTopics, topic)) }
        updateFilteredCounts()
    }

    fun selectSubTopic(subTopic: String) {
        _uiState.update { it.copy(selectedSubTopics = toggleSet(it.selectedSubTopics, subTopic)) }
        updateFilteredCounts()
    }

    fun selectDifficulty(difficulty: String) {
        _uiState.update { it.copy(selectedDifficulties = toggleSet(it.selectedDifficulties, difficulty)) }
        updateFilteredCounts()
    }

    fun toggleExamName(name: String) {
        _uiState.update { it.copy(selectedExamNames = toggleSet(it.selectedExamNames, name)) }
        updateFilteredCounts()
    }

    fun toggleYear(year: String) {
        _uiState.update { it.copy(selectedYears = toggleSet(it.selectedYears, year)) }
        updateFilteredCounts()
    }

    fun toggleShift(shift: String) {
        _uiState.update { it.copy(selectedShifts = toggleSet(it.selectedShifts, shift)) }
        updateFilteredCounts()
    }

    fun toggleTag(tag: String) {
        _uiState.update { it.copy(selectedTags = toggleSet(it.selectedTags, tag)) }
        updateFilteredCounts()
    }

    fun removeTag(tag: String) = toggleTag(tag)
    fun removeExamName(name: String) = toggleExamName(name)
    fun removeYear(year: String) = toggleYear(year)
    fun removeShift(shift: String) = toggleShift(shift)

    // UI State Management
    fun setAdvancedFiltersExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isAdvancedFiltersExpanded = expanded) }
    }

    fun openBottomSheet(type: FilterType) {
        _uiState.update { it.copy(activeBottomSheet = type) }
    }

    fun closeBottomSheet() {
        _uiState.update { it.copy(activeBottomSheet = null) }
    }

    fun selectMode(mode: QuizMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun setQuizName(name: String) {
        _uiState.update { it.copy(quizName = name) }
    }

    fun applyQuickStart(count: Int, mode: QuizMode? = null) {
        _uiState.update { it.copy(questionCount = count, selectedMode = mode ?: it.selectedMode) }
    }

    fun resetFilters() {
        _uiState.update { state ->
            state.copy(
                selectedSubjects = emptySet(),
                selectedTopics = emptySet(),
                selectedSubTopics = emptySet(),
                selectedDifficulties = emptySet(),
                selectedExamNames = emptySet(),
                selectedYears = emptySet(),
                selectedShifts = emptySet(),
                selectedTags = emptySet(),
                selectedMode = QuizMode.LEARNING,
                questionCount = 10,
                quizName = ""
            )
        }
        updateFilteredCounts()
    }

    fun clearAdvancedFilters() {
        _uiState.update { state ->
            state.copy(
                selectedExamNames = emptySet(),
                selectedYears = emptySet(),
                selectedShifts = emptySet(),
                selectedTags = emptySet()
            )
        }
        updateFilteredCounts()
    }

    fun startSession() {
        if (currentFilteredIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingSession = true) }
            try {
                val state = _uiState.value
                val finalIds = currentFilteredIds.take(state.questionCount).toList()
                val quizName = state.quizName.ifEmpty { "Custom Quiz" }
                val mode = state.selectedMode.name.lowercase()

                val filtersJson = JSONObject().apply {
                    if (state.selectedSubjects.isNotEmpty()) put("subjects", JSONArray(state.selectedSubjects))
                    if (state.selectedTopics.isNotEmpty()) put("topics", JSONArray(state.selectedTopics))
                    if (state.selectedSubTopics.isNotEmpty()) put("subTopics", JSONArray(state.selectedSubTopics))
                    if (state.selectedDifficulties.isNotEmpty()) put("difficulties", JSONArray(state.selectedDifficulties))
                }.toString()

                val quizId = quizRepository.createQuiz(
                    quizName = quizName,
                    mode = mode,
                    filters = filtersJson,
                    questionIds = finalIds
                )

                _uiState.update { it.copy(sessionCreatedEvent = quizId) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCreatingSession = false,
                        error = e.localizedMessage ?: "Failed to create quiz"
                    )
                }
            }
        }
    }

    fun onSessionCreatedHandled() {
        _uiState.update { it.copy(sessionCreatedEvent = null, isCreatingSession = false) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}

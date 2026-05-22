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

    // Inverted Index Structure
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

                // Extract filter options dynamically from metadata
                val subjects = allMetadata.map { it.subject }.filter { it.isNotEmpty() }.distinct().sorted()
                val topics = allMetadata.map { it.topic }.filter { it.isNotEmpty() }.distinct().sorted()
                val subTopics = allMetadata.map { it.subTopic }.filter { it.isNotEmpty() }.distinct().sorted()
                val difficulties = allMetadata.map { it.difficulty }.filter { it.isNotEmpty() }.distinct().sorted()
                val examNames = allMetadata.map { it.examName }.filter { it.isNotEmpty() }.distinct().sorted()
                val examYears = allMetadata.map { it.examYear }.filter { it.isNotEmpty() }.distinct().sorted()
                val tags = allMetadata.flatMap { it.tags }.filter { it.isNotEmpty() }.distinct().sorted()

                // Shifts aren't in metadata yet, but preserving structure for future if needed
                val shifts = emptyList<String>()

                val options = QuizFilterOptions(
                    subjects = subjects,
                    topics = topics,
                    subTopics = subTopics,
                    difficulties = difficulties,
                    examNames = examNames,
                    examYears = examYears,
                    shifts = shifts,
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

        // Initialize maps
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

    private fun updateFilteredCounts() {
        val state = _uiState.value

        var currentSet: Set<String>? = null

        fun intersectWith(type: FilterType, values: Set<String>) {
            if (values.isEmpty()) return

            val typeMap = invertedIndex[type] ?: return

            // Union of selected values within the SAME category (e.g., ExamName A OR ExamName B)
            val valuesUnion = mutableSetOf<String>()
            values.forEach { v ->
                typeMap[v]?.let { valuesUnion.addAll(it) }
            }

            // Intersect with global set (Category 1 AND Category 2)
            currentSet = if (currentSet == null) {
                valuesUnion
            } else {
                currentSet!!.intersect(valuesUnion)
            }
        }

        if (state.selectedSubject.isNotEmpty()) intersectWith(FilterType.SUBJECT, setOf(state.selectedSubject))
        if (state.selectedTopic.isNotEmpty()) intersectWith(FilterType.TOPIC, setOf(state.selectedTopic))
        if (state.selectedSubTopic.isNotEmpty()) intersectWith(FilterType.SUB_TOPIC, setOf(state.selectedSubTopic))
        if (state.selectedDifficulty.isNotEmpty()) intersectWith(FilterType.DIFFICULTY, setOf(state.selectedDifficulty))

        intersectWith(FilterType.EXAM_NAME, state.selectedExamNames)
        intersectWith(FilterType.EXAM_YEAR, state.selectedYears)
        intersectWith(FilterType.SHIFT, state.selectedShifts)
        intersectWith(FilterType.TAGS, state.selectedTags)

        currentFilteredIds = currentSet ?: allMetadata.map { it.id }.toSet()

        _uiState.update { it.copy(availableQuestionCount = currentFilteredIds.size) }
    }


    // --- Intent Handlers ---

    fun selectSubject(subject: String) {
        _uiState.update {
            it.copy(
                selectedSubject = subject,
                selectedTopic = "", // Reset dependent
                selectedSubTopic = ""
            )
        }
        updateFilteredCounts()
    }

    fun selectTopic(topic: String) {
        _uiState.update {
            it.copy(
                selectedTopic = topic,
                selectedSubTopic = "" // Reset dependent
            )
        }
        updateFilteredCounts()
    }

    fun selectSubTopic(subTopic: String) {
        _uiState.update {
            it.copy(
                selectedSubTopic = subTopic
            )
        }
        updateFilteredCounts()
    }

    fun selectDifficulty(difficulty: String) {
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
        updateFilteredCounts()
    }

    // Advanced Multi-Select Filters
    fun toggleExamName(name: String) {
        _uiState.update { state ->
            val newSet = if (state.selectedExamNames.contains(name)) {
                state.selectedExamNames - name
            } else {
                state.selectedExamNames + name
            }
            state.copy(selectedExamNames = newSet)
        }
        updateFilteredCounts()
    }

    fun toggleYear(year: String) {
        _uiState.update { state ->
            val newSet = if (state.selectedYears.contains(year)) {
                state.selectedYears - year
            } else {
                state.selectedYears + year
            }
            state.copy(selectedYears = newSet)
        }
        updateFilteredCounts()
    }

    fun toggleShift(shift: String) {
        _uiState.update { state ->
            val newSet = if (state.selectedShifts.contains(shift)) {
                state.selectedShifts - shift
            } else {
                state.selectedShifts + shift
            }
            state.copy(selectedShifts = newSet)
        }
        updateFilteredCounts()
    }

    fun toggleTag(tag: String) {
        _uiState.update { state ->
            val newSet = if (state.selectedTags.contains(tag)) {
                state.selectedTags - tag
            } else {
                state.selectedTags + tag
            }
            state.copy(selectedTags = newSet)
        }
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

    // --- Others ---

    fun selectMode(mode: QuizMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun setQuizName(name: String) {
        _uiState.update { it.copy(quizName = name) }
    }

    fun applyQuickStart(count: Int, mode: QuizMode? = null) {
        _uiState.update { state ->
            state.copy(
                questionCount = count,
                selectedMode = mode ?: state.selectedMode
            )
        }
    }

    fun resetFilters() {
        _uiState.update { state ->
            state.copy(
                selectedSubject = "",
                selectedTopic = "",
                selectedSubTopic = "",
                selectedDifficulty = "",
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

    // --- Session Creation ---
    fun startSession() {
        if (currentFilteredIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingSession = true) }
            try {
                val state = _uiState.value
                val finalIds = currentFilteredIds.take(state.questionCount).toList()
                val quizName = state.quizName.ifEmpty { "Custom Quiz" }
                val mode = state.selectedMode.name.lowercase()

                // Create a JSON string of active filters for history/resuming purposes
                val filtersJson = JSONObject().apply {
                    if (state.selectedSubject.isNotEmpty()) put("subject", state.selectedSubject)
                    if (state.selectedTopic.isNotEmpty()) put("topic", state.selectedTopic)
                    if (state.selectedSubTopic.isNotEmpty()) put("subTopic", state.selectedSubTopic)
                    if (state.selectedDifficulty.isNotEmpty()) put("difficulty", state.selectedDifficulty)
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

package com.cglhustle.feature.quizconfig.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cglhustle.engine.facetedsearch.EngineMetadata
import com.cglhustle.engine.facetedsearch.FilterCategory
import com.cglhustle.engine.facetedsearch.FilterChipState
import com.cglhustle.engine.facetedsearch.FilterQuery
import com.cglhustle.engine.facetedsearch.QueryResult
import com.cglhustle.engine.facetedsearch.QuizSearchEngine
import com.cglhustle.feature.quizconfig.domain.model.QuestionMetadata
import com.cglhustle.feature.quizconfig.domain.model.QuizFilterOptions
import com.cglhustle.feature.quizconfig.domain.model.QuizMode
import com.cglhustle.feature.quizconfig.domain.repository.QuestionMetadataRepository
import com.cglhustle.feature.quizconfig.domain.repository.QuizRepository
import com.cglhustle.feature.quizconfig.ui.state.QuizConfigUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class QuizConfigViewModel @Inject constructor(
    private val metadataRepository: QuestionMetadataRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizConfigUiState())
    val uiState: StateFlow<QuizConfigUiState> = _uiState.asStateFlow()

    private val engine = QuizSearchEngine()
    private val queryFlow = MutableStateFlow(FilterQuery())

    // Internal base options extracted from index
    private var baseOptions: QuizFilterOptions? = null

    init {
        setupQueryPipeline()
        loadMetadata()
    }

    private fun setupQueryPipeline() {
        viewModelScope.launch(Dispatchers.Default) {
            queryFlow.collectLatest { query ->
                // This block automatically cancels on new emissions!
                val result = engine.query(query)

                withContext(Dispatchers.Main) {
                    updateUiStateFromResult(query, result)
                }
            }
        }
    }

    private fun updateUiStateFromResult(query: FilterQuery, result: QueryResult) {
        val options = baseOptions ?: return

        _uiState.update { state ->
            state.copy(
                availableQuestionCount = result.totalMatches,
                subjectsState = buildChipState(FilterCategory.SUBJECT, options.subjects, query, result),
                topicsState = buildChipState(FilterCategory.TOPIC, options.topics, query, result),
                subTopicsState = buildChipState(FilterCategory.SUB_TOPIC, options.subTopics, query, result),
                difficultiesState = buildChipState(FilterCategory.DIFFICULTY, options.difficulties, query, result),

                // Advanced Filters (Still keeping legacy Sets for bottom sheets, but we update the immutable list for rendering)
                examNamesState = buildChipState(FilterCategory.EXAM_NAME, options.examNames, query, result),
                yearsState = buildChipState(FilterCategory.EXAM_YEAR, options.examYears, query, result),
                shiftsState = buildChipState(FilterCategory.SHIFT, options.shifts, query, result),
                tagsState = buildChipState(FilterCategory.TAGS, options.tags, query, result),

                selectedExamNames = query.selections[FilterCategory.EXAM_NAME] ?: emptySet(),
                selectedYears = query.selections[FilterCategory.EXAM_YEAR] ?: emptySet(),
                selectedShifts = query.selections[FilterCategory.SHIFT] ?: emptySet(),
                selectedTags = query.selections[FilterCategory.TAGS] ?: emptySet()
            )
        }
    }

    private fun buildChipState(
        category: FilterCategory,
        allOptions: List<String>,
        query: FilterQuery,
        result: QueryResult
    ): kotlinx.collections.immutable.ImmutableList<FilterChipState> {
        val selections = query.selections[category] ?: emptySet()
        val counts = result.facetCounts[category] ?: emptyMap()
        val visibleValues = result.visibleValues[category]

        return allOptions.map { option ->
            FilterChipState(
                name = option,
                count = counts[option] ?: 0,
                isSelected = selections.contains(option),
                // If visibleValues is null, it means the rule allows everything (or engine doesn't hide it)
                // BUT we need to hide things with count 0 unless they are selected
                isVisible = if (visibleValues != null) {
                    visibleValues.contains(option)
                } else {
                    (counts[option] ?: 0) > 0 || selections.contains(option)
                }
            )
        }.toImmutableList()
    }

    fun loadMetadata() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFilters = true, error = null) }
            try {
                // Network Fetch
                val allMetadata = metadataRepository.fetchMetadata()

                withContext(Dispatchers.Default) {
                    // Convert to Engine Metadata
                    val engineMetadata = allMetadata.map {
                        EngineMetadata(
                            id = it.id,
                            subject = it.subject,
                            topic = it.topic,
                            subTopic = it.subTopic,
                            difficulty = it.difficulty,
                            questionType = it.questionType,
                            examName = it.examName,
                            examYear = it.examYear,
                            tags = it.tags
                        )
                    }

                    // Build Search Engine Index
                    engine.buildIndex(engineMetadata)

                    // Extract unique base options for rendering order
                    val subjects = allMetadata.map { it.subject }.filter { it.isNotBlank() }.distinct().sorted()
                    val topics = allMetadata.map { it.topic }.filter { it.isNotBlank() }.distinct().sorted()
                    val subTopics = allMetadata.map { it.subTopic }.filter { it.isNotBlank() }.distinct().sorted()
                    val difficulties = allMetadata.map { it.difficulty }.filter { it.isNotBlank() }.distinct().sorted()
                    val examNames = allMetadata.map { it.examName }.filter { it.isNotBlank() }.distinct().sorted()
                    val examYears = allMetadata.map { it.examYear }.filter { it.isNotBlank() }.distinct().sorted()
                    val tags = allMetadata.flatMap { it.tags }.distinct().sorted()

                    baseOptions = QuizFilterOptions(
                        subjects = subjects,
                        topics = topics,
                        subTopics = subTopics,
                        difficulties = difficulties,
                        examNames = examNames,
                        examYears = examYears,
                        shifts = emptyList(), // Stubbed
                        tags = tags
                    )
                }

                // Unlock UI immediately
                _uiState.update {
                    it.copy(
                        isLoadingFilters = false,
                        filterOptions = baseOptions
                    )
                }

                // Trigger initial query calculation
                queryFlow.value = FilterQuery()

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingFilters = false, error = e.localizedMessage) }
            }
        }
    }

    // --- Intent Handlers ---

    private fun toggleSelection(category: FilterCategory, value: String) {
        val currentQuery = queryFlow.value
        val currentSelections = currentQuery.selections.toMutableMap()

        val categorySelections = currentSelections[category]?.toMutableSet() ?: mutableSetOf()

        if (categorySelections.contains(value)) {
            categorySelections.remove(value)
        } else {
            categorySelections.add(value)
        }

        if (categorySelections.isEmpty()) {
            currentSelections.remove(category)
        } else {
            currentSelections[category] = categorySelections
        }

        // Enforce Hierarchy deselection rules
        if (category == FilterCategory.SUBJECT && categorySelections.isEmpty()) {
            currentSelections.remove(FilterCategory.TOPIC)
            currentSelections.remove(FilterCategory.SUB_TOPIC)
        } else if (category == FilterCategory.TOPIC && categorySelections.isEmpty()) {
            currentSelections.remove(FilterCategory.SUB_TOPIC)
        }

        queryFlow.value = currentQuery.copy(selections = currentSelections)
    }

    private fun setCategorySelections(category: FilterCategory, values: Set<String>) {
        val currentQuery = queryFlow.value
        val currentSelections = currentQuery.selections.toMutableMap()

        if (values.isEmpty()) {
            currentSelections.remove(category)

            // Hierarchy rules
            if (category == FilterCategory.SUBJECT) {
                currentSelections.remove(FilterCategory.TOPIC)
                currentSelections.remove(FilterCategory.SUB_TOPIC)
            } else if (category == FilterCategory.TOPIC) {
                currentSelections.remove(FilterCategory.SUB_TOPIC)
            }
        } else {
            currentSelections[category] = values
        }

        queryFlow.value = currentQuery.copy(selections = currentSelections)
    }

    fun selectSubject(subject: String) = toggleSelection(FilterCategory.SUBJECT, subject)
    fun selectTopic(topic: String) = toggleSelection(FilterCategory.TOPIC, topic)
    fun selectSubTopic(subTopic: String) = toggleSelection(FilterCategory.SUB_TOPIC, subTopic)
    fun selectDifficulty(difficulty: String) = toggleSelection(FilterCategory.DIFFICULTY, difficulty)
    fun toggleExamName(name: String) = toggleSelection(FilterCategory.EXAM_NAME, name)
    fun toggleYear(year: String) = toggleSelection(FilterCategory.EXAM_YEAR, year)
    fun toggleShift(shift: String) = toggleSelection(FilterCategory.SHIFT, shift)
    fun toggleTag(tag: String) = toggleSelection(FilterCategory.TAGS, tag)

    fun selectAll(category: FilterCategory) {
        // Find all currently visible options for this category and select them
        val state = _uiState.value
        val options = when (category) {
            FilterCategory.SUBJECT -> state.subjectsState
            FilterCategory.TOPIC -> state.topicsState
            FilterCategory.SUB_TOPIC -> state.subTopicsState
            FilterCategory.DIFFICULTY -> state.difficultiesState
            else -> return
        }

        val visibleValues = options.filter { it.isVisible }.map { it.name }.toSet()
        val currentSelected = queryFlow.value.selections[category] ?: emptySet()

        setCategorySelections(category, currentSelected + visibleValues)
    }

    fun clearAll(category: FilterCategory) {
        setCategorySelections(category, emptySet())
    }

    fun removeTag(tag: String) = toggleSelection(FilterCategory.TAGS, tag)
    fun removeExamName(name: String) = toggleSelection(FilterCategory.EXAM_NAME, name)
    fun removeYear(year: String) = toggleSelection(FilterCategory.EXAM_YEAR, year)
    fun removeShift(shift: String) = toggleSelection(FilterCategory.SHIFT, shift)

    // UI State Management
    fun setAdvancedFiltersExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isAdvancedFiltersExpanded = expanded) }
    }

    fun openBottomSheet(type: FilterCategory) {
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
                selectedMode = QuizMode.LEARNING,
                questionCount = 10,
                quizName = ""
            )
        }
        queryFlow.value = FilterQuery()
    }

    fun clearAdvancedFilters() {
        val currentSelections = queryFlow.value.selections.toMutableMap()
        currentSelections.remove(FilterCategory.EXAM_NAME)
        currentSelections.remove(FilterCategory.EXAM_YEAR)
        currentSelections.remove(FilterCategory.SHIFT)
        currentSelections.remove(FilterCategory.TAGS)
        queryFlow.value = queryFlow.value.copy(selections = currentSelections)
    }

    fun startSession() {
        if (_uiState.value.availableQuestionCount == 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingSession = true) }
            try {
                val state = _uiState.value

                // EXTRACT IDS ONLY NOW! On the default dispatcher.
                val finalIds = withContext(Dispatchers.Default) {
                    engine.extractFinalIds(queryFlow.value, state.questionCount)
                }

                val quizName = state.quizName.ifEmpty { "Custom Quiz" }
                val mode = state.selectedMode.name.lowercase()

                val currentSelections = queryFlow.value.selections
                val filtersJson = JSONObject().apply {
                    if (!currentSelections[FilterCategory.SUBJECT].isNullOrEmpty()) put("subjects", JSONArray(currentSelections[FilterCategory.SUBJECT]))
                    if (!currentSelections[FilterCategory.TOPIC].isNullOrEmpty()) put("topics", JSONArray(currentSelections[FilterCategory.TOPIC]))
                    if (!currentSelections[FilterCategory.SUB_TOPIC].isNullOrEmpty()) put("subTopics", JSONArray(currentSelections[FilterCategory.SUB_TOPIC]))
                    if (!currentSelections[FilterCategory.DIFFICULTY].isNullOrEmpty()) put("difficulties", JSONArray(currentSelections[FilterCategory.DIFFICULTY]))
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

package com.cglhustle.feature.quizconfig.ui.viewmodel

import android.util.Log
import android.os.SystemClock

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
    private val queryFlow = kotlinx.coroutines.flow.MutableSharedFlow<FilterQuery>(replay = 1, onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST)

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
                Log.d("QuizSearchEngine", "[ENGINE_QUERY] Starting query")
                val queryStart = SystemClock.elapsedRealtime()
                val result = engine.query(query)
                val queryDuration = SystemClock.elapsedRealtime() - queryStart
                Log.d("QuizSearchEngine", "[ENGINE_QUERY] Complete in ${queryDuration}ms. Matches: ${result.totalMatches}")

                withContext(Dispatchers.Main) {
                    updateUiStateFromResult(query, result)
                }
            }
        }
    }

    private fun updateUiStateFromResult(query: FilterQuery, result: QueryResult) {
        val options = baseOptions ?: return

        val mappingStart = SystemClock.elapsedRealtime()

        Log.d("QuizSearchEngine", "[DEBUG_UI] ---- STATE MAPPING START ----")
        Log.d("QuizSearchEngine", "[DEBUG_UI] facetCounts keys: ${result.facetCounts.keys}")
        Log.d("QuizSearchEngine", "[DEBUG_UI] visibleValues keys: ${result.visibleValues.keys}")
        Log.d("QuizSearchEngine", "[DEBUG_UI] totalMatches: ${result.totalMatches}")

        val subjectsState = buildChipState(FilterCategory.SUBJECT, options.subjects, query, result)
        val topicsState = buildChipState(FilterCategory.TOPIC, options.topics, query, result)
        val subTopicsState = buildChipState(FilterCategory.SUB_TOPIC, options.subTopics, query, result)
        val difficultiesState = buildChipState(FilterCategory.DIFFICULTY, options.difficulties, query, result)

        val examNamesState = buildChipState(FilterCategory.EXAM_NAME, options.examNames, query, result)
        val yearsState = buildChipState(FilterCategory.EXAM_YEAR, options.examYears, query, result)
        val shiftsState = buildChipState(FilterCategory.SHIFT, options.shifts, query, result)
        val tagsState = buildChipState(FilterCategory.TAGS, options.tags, query, result)

        Log.d("QuizSearchEngine", "[DEBUG_UI] subjectsState size: ${subjectsState.size}, visible: ${subjectsState.count { it.isVisible }}")
        Log.d("QuizSearchEngine", "[DEBUG_UI] topicsState size: ${topicsState.size}, visible: ${topicsState.count { it.isVisible }}")
        Log.d("QuizSearchEngine", "[DEBUG_UI] subTopicsState size: ${subTopicsState.size}, visible: ${subTopicsState.count { it.isVisible }}")
        Log.d("QuizSearchEngine", "[DEBUG_UI] difficultyState size: ${difficultiesState.size}, visible: ${difficultiesState.count { it.isVisible }}")

        Log.d("QuizSearchEngine", "[DEBUG_UI] [UI_STATE_EMIT] Emitting UI state for query result Reset")

        val mappingDuration = SystemClock.elapsedRealtime() - mappingStart
        Log.d("QuizSearchEngine", "[DEBUG_UI] UI Mapping took ${mappingDuration}ms")

        _uiState.update { state ->
            state.copy(
                availableQuestionCount = result.totalMatches,
                subjectsState = subjectsState,
                topicsState = topicsState,
                subTopicsState = subTopicsState,
                difficultiesState = difficultiesState,
                examNamesState = examNamesState,
                yearsState = yearsState,
                shiftsState = shiftsState,
                tagsState = tagsState,
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
                isVisible = true // [DEBUG] Forced true for UI pipeline testing
            )
        }.toImmutableList()
    }

    fun loadMetadata() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFilters = true, error = null) }
                val metadataStart = SystemClock.elapsedRealtime()
                Log.d("QuizSearchEngine", "[DEBUG_TIMING] Starting metadata fetch")
                try {
                // Network Fetch
                Log.d("QuizSearchEngine", "[METADATA_FETCH] Starting network fetch")
                val fetchStart = SystemClock.elapsedRealtime()
                val allMetadata = metadataRepository.fetchMetadata()
                val fetchDuration = SystemClock.elapsedRealtime() - fetchStart
                Log.d("QuizSearchEngine", "[METADATA_FETCH] Complete. Fetched ${allMetadata.size} rows in ${fetchDuration}ms")

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
                    Log.d("QuizSearchEngine", "[ENGINE_BUILD_START] Starting index build")
                    val buildStart = SystemClock.elapsedRealtime()
                    engine.buildIndex(engineMetadata)
                    val buildDuration = SystemClock.elapsedRealtime() - buildStart
                    Log.d("QuizSearchEngine", "[ENGINE_BUILD_COMPLETE] Complete. Built in ${buildDuration}ms")

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
                queryFlow.tryEmit(FilterQuery())

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingFilters = false, error = e.localizedMessage) }
            }
        }
    }

    // --- Intent Handlers ---

    private fun toggleSelection(category: FilterCategory, value: String) {
        val currentQuery = queryFlow.replayCache.firstOrNull() ?: FilterQuery()
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

        queryFlow.tryEmit(currentQuery.copy(selections = currentSelections))
    }

    private fun setCategorySelections(category: FilterCategory, values: Set<String>) {
        val currentQuery = queryFlow.replayCache.firstOrNull() ?: FilterQuery()
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

        queryFlow.tryEmit(currentQuery.copy(selections = currentSelections))
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
        val currentSelected = (queryFlow.replayCache.firstOrNull() ?: FilterQuery()).selections[category] ?: emptySet()

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
        Log.d("QuizSearchEngine", "[UI_STATE_EMIT] Emitting UI state for query result Reset")
        _uiState.update { state ->
            state.copy(
                selectedMode = QuizMode.LEARNING,
                questionCount = 10,
                quizName = ""
            )
        }
        queryFlow.tryEmit(FilterQuery())
    }

    fun clearAdvancedFilters() {
        val currentQuery = queryFlow.replayCache.firstOrNull() ?: FilterQuery()
        val currentSelections = currentQuery.selections.toMutableMap()
        currentSelections.remove(FilterCategory.EXAM_NAME)
        currentSelections.remove(FilterCategory.EXAM_YEAR)
        currentSelections.remove(FilterCategory.SHIFT)
        currentSelections.remove(FilterCategory.TAGS)
        queryFlow.tryEmit(currentQuery.copy(selections = currentSelections))
    }

    fun startSession() {
        if (_uiState.value.availableQuestionCount == 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingSession = true) }
            try {
                val state = _uiState.value

                // EXTRACT IDS ONLY NOW! On the default dispatcher.
                val finalIds = withContext(Dispatchers.Default) {
                    engine.extractFinalIds(queryFlow.replayCache.firstOrNull() ?: FilterQuery(), state.questionCount)
                }

                val quizName = state.quizName.ifEmpty { "Custom Quiz" }
                val mode = state.selectedMode.name.lowercase()

                val currentSelections = (queryFlow.replayCache.firstOrNull() ?: FilterQuery()).selections
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

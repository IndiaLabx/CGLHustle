package com.cglhustle.feature.quizconfig.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cglhustle.feature.quizconfig.domain.model.QuizConfigPayload
import com.cglhustle.feature.quizconfig.domain.model.QuizMode
import com.cglhustle.feature.quizconfig.domain.repository.QuizConfigRepository
import com.cglhustle.feature.quizconfig.ui.state.FilterType
import com.cglhustle.feature.quizconfig.ui.state.QuizConfigUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizConfigViewModel @Inject constructor(
    private val repository: QuizConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizConfigUiState())
    val uiState: StateFlow<QuizConfigUiState> = _uiState.asStateFlow()

    init {
        loadFilters()
    }

    fun loadFilters() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFilters = true, error = null) }
            try {
                val filters = repository.fetchAvailableFilters()
                _uiState.update {
                    it.copy(
                        isLoadingFilters = false,
                        filterOptions = filters,
                        availableQuestionCount = calculateMockAvailableCount()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingFilters = false,
                        error = e.localizedMessage ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    // --- Intent Handlers ---

    fun selectSubject(subject: String) {
        _uiState.update {
            it.copy(
                selectedSubject = subject,
                selectedTopic = "", // Reset dependent
                selectedSubTopic = "",
                availableQuestionCount = calculateMockAvailableCount(subject = subject)
            )
        }
    }

    fun selectTopic(topic: String) {
        _uiState.update {
            it.copy(
                selectedTopic = topic,
                selectedSubTopic = "", // Reset dependent
                availableQuestionCount = calculateMockAvailableCount(topic = topic)
            )
        }
    }

    fun selectSubTopic(subTopic: String) {
        _uiState.update {
            it.copy(
                selectedSubTopic = subTopic,
                availableQuestionCount = calculateMockAvailableCount(subTopic = subTopic)
            )
        }
    }

    fun selectDifficulty(difficulty: String) {
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
    }

    // Advanced Multi-Select Filters
    fun toggleExamName(name: String) {
        _uiState.update { state ->
            val newSet = if (state.selectedExamNames.contains(name)) {
                state.selectedExamNames - name
            } else {
                state.selectedExamNames + name
            }
            state.copy(
                selectedExamNames = newSet,
                availableQuestionCount = calculateMockAvailableCount(examNames = newSet)
            )
        }
    }

    fun toggleYear(year: String) {
        _uiState.update { state ->
            val newSet = if (state.selectedYears.contains(year)) {
                state.selectedYears - year
            } else {
                state.selectedYears + year
            }
            state.copy(
                selectedYears = newSet,
                availableQuestionCount = calculateMockAvailableCount(years = newSet)
            )
        }
    }

    fun toggleShift(shift: String) {
        _uiState.update { state ->
            val newSet = if (state.selectedShifts.contains(shift)) {
                state.selectedShifts - shift
            } else {
                state.selectedShifts + shift
            }
            state.copy(
                selectedShifts = newSet,
                availableQuestionCount = calculateMockAvailableCount(shifts = newSet)
            )
        }
    }

    fun toggleTag(tag: String) {
        _uiState.update { state ->
            val newSet = if (state.selectedTags.contains(tag)) {
                state.selectedTags - tag
            } else {
                state.selectedTags + tag
            }
            state.copy(
                selectedTags = newSet,
                availableQuestionCount = calculateMockAvailableCount(tags = newSet)
            )
        }
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
                quizName = "",
                availableQuestionCount = calculateMockAvailableCount()
            )
        }
    }

    fun clearAdvancedFilters() {
        _uiState.update { state ->
            state.copy(
                selectedExamNames = emptySet(),
                selectedYears = emptySet(),
                selectedShifts = emptySet(),
                selectedTags = emptySet(),
                availableQuestionCount = calculateMockAvailableCount(
                    examNames = emptySet(),
                    years = emptySet(),
                    shifts = emptySet(),
                    tags = emptySet()
                )
            )
        }
    }

    // --- Mock Count Calculation ---
    private fun calculateMockAvailableCount(
        subject: String = _uiState.value.selectedSubject,
        topic: String = _uiState.value.selectedTopic,
        subTopic: String = _uiState.value.selectedSubTopic,
        examNames: Set<String> = _uiState.value.selectedExamNames,
        years: Set<String> = _uiState.value.selectedYears,
        shifts: Set<String> = _uiState.value.selectedShifts,
        tags: Set<String> = _uiState.value.selectedTags
    ): Int {
        var count = 12482
        if (subject.isNotEmpty()) count = (count * 0.4).toInt()
        if (topic.isNotEmpty()) count = (count * 0.3).toInt()
        if (subTopic.isNotEmpty()) count = (count * 0.2).toInt()

        // Multi-select filters typically narrow the search space further if present
        if (examNames.isNotEmpty()) count = (count * 0.6).toInt()
        if (years.isNotEmpty()) count = (count * 0.8).toInt()
        if (shifts.isNotEmpty()) count = (count * 0.9).toInt()
        if (tags.isNotEmpty()) count = (count * 0.7).toInt()

        return maxOf(count, 0)
    }

    // --- Session Creation ---
    fun startSession() {
        val state = _uiState.value
        val payload = QuizConfigPayload(
            subjects = listOfNotNull(state.selectedSubject.ifEmpty { null }),
            topics = listOfNotNull(state.selectedTopic.ifEmpty { null }),
            subTopics = listOfNotNull(state.selectedSubTopic.ifEmpty { null }),
            difficulty = state.selectedDifficulty,
            examNames = state.selectedExamNames.toList(),
            examYears = state.selectedYears.toList(),
            shifts = state.selectedShifts.toList(),
            tags = state.selectedTags.toList(),
            mode = state.selectedMode,
            questionCount = state.questionCount,
            quizName = state.quizName.ifEmpty { null }
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingSession = true) }
            try {
                val sessionId = repository.createSession(payload)
                _uiState.update { it.copy(sessionCreatedEvent = sessionId) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCreatingSession = false,
                        error = e.localizedMessage ?: "Failed to start session"
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

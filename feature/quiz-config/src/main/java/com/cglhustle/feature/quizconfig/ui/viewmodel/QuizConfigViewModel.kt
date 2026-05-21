package com.cglhustle.feature.quizconfig.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cglhustle.feature.quizconfig.domain.model.QuizConfigPayload
import com.cglhustle.feature.quizconfig.domain.model.QuizMode
import com.cglhustle.feature.quizconfig.domain.repository.QuizConfigRepository
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
                availableQuestionCount = calculateMockAvailableCount(subject)
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

    fun selectYear(year: String) {
        _uiState.update { it.copy(selectedYear = year) }
    }

    fun selectShift(shift: String) {
        _uiState.update { it.copy(selectedShift = shift) }
    }

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
                selectedYear = "",
                selectedShift = "",
                selectedMode = QuizMode.LEARNING,
                questionCount = 10,
                quizName = "",
                availableQuestionCount = calculateMockAvailableCount()
            )
        }
    }

    // --- Mock Count Calculation ---
    private fun calculateMockAvailableCount(
        subject: String = _uiState.value.selectedSubject,
        topic: String = _uiState.value.selectedTopic,
        subTopic: String = _uiState.value.selectedSubTopic
    ): Int {
        // Simple mock logic to simulate reactive counts
        var count = 12482
        if (subject.isNotEmpty()) count = (count * 0.4).toInt()
        if (topic.isNotEmpty()) count = (count * 0.3).toInt()
        if (subTopic.isNotEmpty()) count = (count * 0.2).toInt()
        return maxOf(count, 0)
    }

    // --- Session Creation ---

    fun startSession() {
        val state = _uiState.value
        val payload = QuizConfigPayload(
            subject = state.selectedSubject,
            topic = state.selectedTopic,
            subTopic = state.selectedSubTopic.ifEmpty { null },
            difficulty = state.selectedDifficulty,
            examYear = state.selectedYear,
            shift = state.selectedShift,
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

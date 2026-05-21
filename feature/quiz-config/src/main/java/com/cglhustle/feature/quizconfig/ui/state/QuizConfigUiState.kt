package com.cglhustle.feature.quizconfig.ui.state

import com.cglhustle.feature.quizconfig.domain.model.QuizFilterOptions
import com.cglhustle.feature.quizconfig.domain.model.QuizMode

data class QuizConfigUiState(
    val isLoadingFilters: Boolean = true,
    val filterOptions: QuizFilterOptions? = null,
    val error: String? = null,

    // Selected Filters
    val selectedSubject: String = "",
    val selectedTopic: String = "",
    val selectedSubTopic: String = "",
    val selectedDifficulty: String = "",
    val selectedYear: String = "",
    val selectedShift: String = "",

    // Mode & Settings
    val selectedMode: QuizMode = QuizMode.LEARNING,
    val questionCount: Int = 10,
    val quizName: String = "",

    // Dynamic State
    val availableQuestionCount: Int = 0,

    val isCreatingSession: Boolean = false,
    val sessionCreatedEvent: String? = null
)

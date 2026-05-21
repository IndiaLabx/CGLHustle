package com.cglhustle.feature.quizconfig.ui.state

import com.cglhustle.feature.quizconfig.domain.model.QuizFilterOptions
import com.cglhustle.feature.quizconfig.domain.model.QuizMode

enum class FilterType {
    SUBJECT,
    TOPIC,
    SUB_TOPIC,
    EXAM_NAME,
    EXAM_YEAR,
    SHIFT,
    TAGS
}

data class QuizConfigUiState(
    val isLoadingFilters: Boolean = true,
    val filterOptions: QuizFilterOptions? = null,
    val error: String? = null,

    // Primary Filters (Single Select for Phase 1 compatibility, can be multi later)
    val selectedSubject: String = "",
    val selectedTopic: String = "",
    val selectedSubTopic: String = "",
    val selectedDifficulty: String = "",

    // Advanced Filters (Multi-Select)
    val selectedExamNames: Set<String> = emptySet(),
    val selectedYears: Set<String> = emptySet(),
    val selectedShifts: Set<String> = emptySet(),
    val selectedTags: Set<String> = emptySet(),

    // Expansion & Bottom Sheet State
    val isAdvancedFiltersExpanded: Boolean = false,
    val activeBottomSheet: FilterType? = null,

    // Mode & Settings
    val selectedMode: QuizMode = QuizMode.LEARNING,
    val questionCount: Int = 10,
    val quizName: String = "",

    // Dynamic State
    val availableQuestionCount: Int = 0,

    val isCreatingSession: Boolean = false,
    val sessionCreatedEvent: String? = null
)

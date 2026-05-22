package com.cglhustle.feature.quizconfig.ui.state

import com.cglhustle.feature.quizconfig.domain.model.QuizFilterOptions
import com.cglhustle.feature.quizconfig.domain.model.QuizMode

enum class FilterType {
    SUBJECT,
    TOPIC,
    SUB_TOPIC,
    DIFFICULTY,
    EXAM_NAME,
    EXAM_YEAR,
    SHIFT,
    TAGS
}

data class QuizConfigUiState(
    val isLoadingFilters: Boolean = true,
    val filterOptions: QuizFilterOptions? = null,
    val error: String? = null,

    // Primary Filters (Now True Multi-Select)
    val selectedSubjects: Set<String> = emptySet(),
    val selectedTopics: Set<String> = emptySet(),
    val selectedSubTopics: Set<String> = emptySet(),
    val selectedDifficulties: Set<String> = emptySet(),

    // Advanced Filters (Multi-Select)
    val selectedExamNames: Set<String> = emptySet(),
    val selectedYears: Set<String> = emptySet(),
    val selectedShifts: Set<String> = emptySet(),
    val selectedTags: Set<String> = emptySet(),

    // Expansion & Bottom Sheet State
    val isAdvancedFiltersExpanded: Boolean = false,
    val activeBottomSheet: FilterType? = null,

    // Dynamic Filter Counts Map: Category -> Value -> Count
    val dynamicCounts: Map<FilterType, Map<String, Int>> = emptyMap(),

    // Mode & Settings
    val selectedMode: QuizMode = QuizMode.LEARNING,
    val questionCount: Int = 10,
    val quizName: String = "",

    // Dynamic State
    val availableQuestionCount: Int = 0,

    val isCreatingSession: Boolean = false,
    val sessionCreatedEvent: String? = null
)

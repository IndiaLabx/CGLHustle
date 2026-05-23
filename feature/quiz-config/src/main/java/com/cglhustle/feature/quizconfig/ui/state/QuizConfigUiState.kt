package com.cglhustle.feature.quizconfig.ui.state

import com.cglhustle.engine.facetedsearch.FilterCategory
import com.cglhustle.engine.facetedsearch.FilterChipState
import com.cglhustle.feature.quizconfig.domain.model.QuizFilterOptions
import com.cglhustle.feature.quizconfig.domain.model.QuizMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class QuizConfigUiState(
    val isLoadingFilters: Boolean = true,
    val filterOptions: QuizFilterOptions? = null,
    val error: String? = null,

    // Primary Filters (Render State)
    val subjectsState: ImmutableList<FilterChipState> = persistentListOf(),
    val topicsState: ImmutableList<FilterChipState> = persistentListOf(),
    val subTopicsState: ImmutableList<FilterChipState> = persistentListOf(),
    val difficultiesState: ImmutableList<FilterChipState> = persistentListOf(),

    // Advanced Filters (Render State)
    val examNamesState: ImmutableList<FilterChipState> = persistentListOf(),
    val yearsState: ImmutableList<FilterChipState> = persistentListOf(),
    val shiftsState: ImmutableList<FilterChipState> = persistentListOf(),
    val tagsState: ImmutableList<FilterChipState> = persistentListOf(),

    // Legacy structures kept temporarily for BottomSheets/Advanced section components that haven't been fully refactored to ImmutableList yet
    val selectedExamNames: Set<String> = emptySet(),
    val selectedYears: Set<String> = emptySet(),
    val selectedShifts: Set<String> = emptySet(),
    val selectedTags: Set<String> = emptySet(),

    // Expansion & Bottom Sheet State
    val isAdvancedFiltersExpanded: Boolean = false,
    val activeBottomSheet: FilterCategory? = null,

    // Mode & Settings
    val selectedMode: QuizMode = QuizMode.LEARNING,
    val questionCount: Int = 10,
    val quizName: String = "",

    // Dynamic State
    val availableQuestionCount: Int = 0,

    val isCreatingSession: Boolean = false,
    val sessionCreatedEvent: String? = null
)

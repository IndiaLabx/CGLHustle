package com.cglhustle.feature.quizconfig.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cglhustle.core.ui.theme.AppSpacing
import androidx.hilt.navigation.compose.hiltViewModel
import com.cglhustle.feature.quizconfig.ui.components.*
import com.cglhustle.engine.facetedsearch.FilterCategory
import com.cglhustle.feature.quizconfig.ui.viewmodel.QuizConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizConfigScreen(
    onConfigComplete: (String) -> Unit,
    viewModel: QuizConfigViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()

    LaunchedEffect(uiState.sessionCreatedEvent) {
        uiState.sessionCreatedEvent?.let { sessionId ->
            onConfigComplete(sessionId)
            viewModel.onSessionCreatedHandled()
        }
    }

    // Advanced Filter Bottom Sheets
    if (uiState.activeBottomSheet != null && uiState.filterOptions != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        when (uiState.activeBottomSheet) {
            FilterCategory.EXAM_NAME -> {
                MultiSelectBottomSheet(
                    title = "Select Exam Names",
                    options = uiState.filterOptions!!.examNames,
                    selectedOptions = uiState.selectedExamNames,
                    onToggleOption = viewModel::toggleExamName,
                    onClearAll = viewModel::clearAdvancedFilters,
                    onDismiss = viewModel::closeBottomSheet,
                    sheetState = sheetState
                )
            }
            FilterCategory.EXAM_YEAR -> {
                MultiSelectBottomSheet(
                    title = "Select Exam Years",
                    options = uiState.filterOptions!!.examYears,
                    selectedOptions = uiState.selectedYears,
                    onToggleOption = viewModel::toggleYear,
                    onClearAll = { },
                    onDismiss = viewModel::closeBottomSheet,
                    sheetState = sheetState
                )
            }
            FilterCategory.SHIFT -> {
                MultiSelectBottomSheet(
                    title = "Select Shifts",
                    options = uiState.filterOptions!!.shifts,
                    selectedOptions = uiState.selectedShifts,
                    onToggleOption = viewModel::toggleShift,
                    onClearAll = { },
                    onDismiss = viewModel::closeBottomSheet,
                    sheetState = sheetState
                )
            }
            FilterCategory.TAGS -> {
                MultiSelectBottomSheet(
                    title = "Select Tags",
                    options = uiState.filterOptions!!.tags,
                    selectedOptions = uiState.selectedTags,
                    onToggleOption = viewModel::toggleTag,
                    onClearAll = { },
                    onDismiss = viewModel::closeBottomSheet,
                    sheetState = sheetState
                )
            }
            else -> {
                viewModel.closeBottomSheet()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(
            mode = uiState.selectedMode,
            scrollState = scrollState
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                QuizConfigTopBar(
                    availableQuestionCount = uiState.availableQuestionCount,
                    onNavigateBack = { /* Normally handled by navController */ }
                )
            },
            bottomBar = {
                if (!uiState.isLoadingFilters && uiState.error == null) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.availableQuestionCount == 0) {
                            EmptyStateWarning()
                        }
                        StickyCreateQuizBar(
                            quizName = uiState.quizName,
                            onQuizNameChange = viewModel::setQuizName,
                            onReset = viewModel::resetFilters,
                            onCreateQuiz = viewModel::startSession,
                            availableQuestionCount = uiState.availableQuestionCount,
                            isCreating = uiState.isCreatingSession
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoadingFilters) {
                    ShimmerSkeleton(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(AppSpacing.ScreenPadding)
                    )
                } else if (uiState.error != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = uiState.error ?: "An unexpected error occurred",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadMetadata() }) {
                            Text("Retry")
                        }
                    }
                } else {
                    uiState.filterOptions?.let { options ->
                        LazyColumn(
                            state = scrollState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = AppSpacing.ScreenPadding, vertical = AppSpacing.sm),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.SectionSpacing)
                        ) {
                            item {
                                ModeSwitcher(
                                    selectedMode = uiState.selectedMode,
                                    onModeSelected = viewModel::selectMode
                                )
                            }

                            item {
                                QuickStartButtons(
                                    onQuickStart = viewModel::applyQuickStart
                                )
                            }

                            item {
                                ActiveFiltersSection(
                                    subjectsState = uiState.subjectsState,
                                    topicsState = uiState.topicsState,
                                    subTopicsState = uiState.subTopicsState,
                                    difficultiesState = uiState.difficultiesState,
                                    onRemoveSubject = viewModel::selectSubject,
                                    onRemoveTopic = viewModel::selectTopic,
                                    onRemoveSubTopic = viewModel::selectSubTopic,
                                    onRemoveDifficulty = viewModel::selectDifficulty
                                )
                            }

                            item {
                                ClassificationSection(
                                    subjectsState = uiState.subjectsState,
                                    topicsState = uiState.topicsState,
                                    subTopicsState = uiState.subTopicsState,
                                    onSubjectSelected = viewModel::selectSubject,
                                    onTopicSelected = viewModel::selectTopic,
                                    onSubTopicSelected = viewModel::selectSubTopic,
                                    onSelectAll = viewModel::selectAll,
                                    onClearAll = viewModel::clearAll
                                )
                            }

                            item {
                                DifficultySelector(
                                    difficultiesState = uiState.difficultiesState,
                                    onDifficultySelected = viewModel::selectDifficulty
                                )
                            }

                            item {
                                AdvancedFiltersSection(
                                    isExpanded = uiState.isAdvancedFiltersExpanded,
                                    onToggleExpand = { viewModel.setAdvancedFiltersExpanded(!uiState.isAdvancedFiltersExpanded) },
                                    selectedExamNames = uiState.selectedExamNames,
                                    selectedYears = uiState.selectedYears,
                                    selectedShifts = uiState.selectedShifts,
                                    selectedTags = uiState.selectedTags,
                                    onOpenFilterSheet = viewModel::openBottomSheet,
                                    onRemoveExamName = viewModel::removeExamName,
                                    onRemoveYear = viewModel::removeYear,
                                    onRemoveShift = viewModel::removeShift,
                                    onRemoveTag = viewModel::removeTag
                                )
                            }

                            item {
                                QuestionTypeSelector()
                            }

                            // Bottom padding for the sticky bottom bar & empty warning
                            item {
                                Spacer(modifier = Modifier.height(140.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

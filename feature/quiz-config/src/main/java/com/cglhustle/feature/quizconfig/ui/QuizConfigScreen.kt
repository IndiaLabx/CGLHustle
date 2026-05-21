package com.cglhustle.feature.quizconfig.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cglhustle.feature.quizconfig.ui.components.*
import com.cglhustle.feature.quizconfig.ui.state.FilterType
import com.cglhustle.feature.quizconfig.ui.viewmodel.QuizConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizConfigScreen(
    onConfigComplete: (String) -> Unit,
    viewModel: QuizConfigViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
            FilterType.EXAM_NAME -> {
                MultiSelectBottomSheet(
                    title = "Select Exam Names",
                    options = uiState.filterOptions!!.examNames,
                    selectedOptions = uiState.selectedExamNames,
                    onToggleOption = viewModel::toggleExamName,
                    onClearAll = viewModel::clearAdvancedFilters, // We might want a specific clear, but reuse clearAdvancedFilters for now or just let them uncheck. Simple approach: let them uncheck. Actually I will provide a no-op for now.
                    onDismiss = viewModel::closeBottomSheet,
                    sheetState = sheetState
                )
            }
            FilterType.EXAM_YEAR -> {
                MultiSelectBottomSheet(
                    title = "Select Exam Years",
                    options = uiState.filterOptions!!.examYears,
                    selectedOptions = uiState.selectedYears,
                    onToggleOption = viewModel::toggleYear,
                    onClearAll = { /* specific clear logic if desired */ },
                    onDismiss = viewModel::closeBottomSheet,
                    sheetState = sheetState
                )
            }
            FilterType.SHIFT -> {
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
            FilterType.TAGS -> {
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
                // If primary filters evolve to bottom sheets, handle here
                viewModel.closeBottomSheet()
            }
        }
    }

    Scaffold(
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Loading filters...")
                    LinearProgressIndicator()
                }
            } else if (uiState.error != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = uiState.error ?: "An unexpected error occurred",
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = { viewModel.loadFilters() }) {
                        Text("Retry")
                    }
                }
            } else {
                uiState.filterOptions?.let { options ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
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
                            ClassificationSection(
                                subjects = options.subjects,
                                topics = options.topics,
                                subTopics = options.subTopics,
                                selectedSubject = uiState.selectedSubject,
                                selectedTopic = uiState.selectedTopic,
                                selectedSubTopic = uiState.selectedSubTopic,
                                onSubjectSelected = viewModel::selectSubject,
                                onTopicSelected = viewModel::selectTopic,
                                onSubTopicSelected = viewModel::selectSubTopic
                            )
                        }

                        item {
                            DifficultySelector(
                                difficulties = options.difficulties,
                                selectedDifficulty = uiState.selectedDifficulty,
                                onDifficultySelected = viewModel::selectDifficulty
                            )
                        }

                        // Phase 2: Advanced Filters Section
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

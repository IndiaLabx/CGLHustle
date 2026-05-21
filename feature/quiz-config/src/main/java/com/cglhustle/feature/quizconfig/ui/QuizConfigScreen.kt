package com.cglhustle.feature.quizconfig.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cglhustle.feature.quizconfig.domain.model.QuizFilterOptions
import com.cglhustle.feature.quizconfig.ui.components.*
import com.cglhustle.feature.quizconfig.ui.viewmodel.QuizConfigViewModel

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

    Scaffold(
        topBar = {
            QuizConfigTopBar(
                availableQuestionCount = uiState.availableQuestionCount,
                onNavigateBack = { /* Normally handled by navController if passed, but left empty for now to match current sig */ }
            )
        },
        bottomBar = {
            if (!uiState.isLoadingFilters && uiState.error == null) {
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
                            .padding(16.dp),
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

                        item {
                            QuestionTypeSelector()
                        }

                        // Bottom padding for the sticky bottom bar
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

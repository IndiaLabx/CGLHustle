package com.cglhustle.feature.quizconfig.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cglhustle.feature.quizconfig.domain.model.QuizConfigPayload

@Composable
fun QuizConfigScreen(
    onConfigComplete: (String) -> Unit,
    viewModel: QuizConfigViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isCreatingSession by viewModel.isCreatingSession.collectAsState()
    val sessionCreatedEvent by viewModel.sessionCreatedEvent.collectAsState()

    LaunchedEffect(sessionCreatedEvent) {
        sessionCreatedEvent?.let { sessionId ->
            onConfigComplete(sessionId)
            viewModel.onSessionCreatedHandled()
        }
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("Quiz Configuration") })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is QuizConfigUiState.Loading -> {
                    // Loading Skeleton (using a simple placeholder for now, avoid infinite spinner per guidelines)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Loading filters...")
                        LinearProgressIndicator()
                    }
                }
                is QuizConfigUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadFilters() }) {
                            Text("Retry")
                        }
                    }
                }
                is QuizConfigUiState.Success -> {
                    if (isCreatingSession) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Starting Quiz Session...")
                            LinearProgressIndicator()
                        }
                    } else {
                        QuizConfigContent(
                            options = state.filters,
                            onStartQuiz = { payload ->
                                viewModel.startSession(payload)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizConfigContent(
    options: com.cglhustle.feature.quizconfig.domain.model.QuizFilterOptions,
    onStartQuiz: (QuizConfigPayload) -> Unit
) {
    var selectedSubject by remember { mutableStateOf(options.subjects.firstOrNull() ?: "") }
    var selectedTopic by remember { mutableStateOf(options.topics.firstOrNull() ?: "") }
    var selectedDifficulty by remember { mutableStateOf(options.difficulties.firstOrNull() ?: "") }
    var selectedYear by remember { mutableStateOf(options.examYears.firstOrNull() ?: "") }
    var selectedShift by remember { mutableStateOf(options.shifts.firstOrNull() ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DropdownSelector("Subject", options.subjects, selectedSubject) { selectedSubject = it }
        DropdownSelector("Topic", options.topics, selectedTopic) { selectedTopic = it }
        DropdownSelector("Difficulty", options.difficulties, selectedDifficulty) { selectedDifficulty = it }
        DropdownSelector("Exam Year", options.examYears, selectedYear) { selectedYear = it }
        DropdownSelector("Shift", options.shifts, selectedShift) { selectedShift = it }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                onStartQuiz(
                    QuizConfigPayload(
                        subject = selectedSubject,
                        topic = selectedTopic,
                        difficulty = selectedDifficulty,
                        examYear = selectedYear,
                        shift = selectedShift
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Quiz")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

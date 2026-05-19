package com.cglhustle.feature.activesession

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cglhustle.core.ui.components.StatefulScreenWrapper
import com.cglhustle.feature.activesession.domain.ActiveSessionData
import com.cglhustle.feature.activesession.domain.SessionStatus

@Composable
fun ActiveSessionRoute(
    sessionId: String? = null,
    onSessionComplete: (String) -> Unit,
    viewModel: ActiveSessionViewModel = hiltViewModel()
) {
    LaunchedEffect(sessionId) {
        viewModel.initialize(sessionId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ActiveSessionEvent.SessionCompleted -> onSessionComplete(event.sessionId)
                is ActiveSessionEvent.ShowSnackbar -> {
                    // Handle snackbar
                }
            }
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    StatefulScreenWrapper(uiState = uiState) { data ->
        ActiveSessionScreen(
            data = data,
            onOptionSelected = viewModel::selectOption,
            onQuestionNavigate = viewModel::navigateToQuestion,
            onPauseToggle = viewModel::togglePause,
            onSubmitSession = { viewModel.submitSession() }
        )
    }
}

@Composable
fun ActiveSessionScreen(
    data: ActiveSessionData,
    onOptionSelected: (String, String) -> Unit,
    onQuestionNavigate: (Int) -> Unit,
    onPauseToggle: () -> Unit,
    onSubmitSession: () -> Unit
) {
    if (data.status == SessionStatus.PAUSED) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Session Paused", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.padding(16.dp))
                Button(onClick = onPauseToggle) {
                    Text("Resume")
                }
            }
        }
        return
    }

    if (data.status == SessionStatus.SUBMITTING) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.padding(16.dp))
                Text("Submitting Session...")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onPauseToggle) {
                Text("Pause")
            }
            Button(onClick = onSubmitSession) {
                Text("Submit Session")
            }
        }

        Spacer(modifier = Modifier.padding(8.dp))

        // Question Navigator
        Text("Questions", style = MaterialTheme.typography.titleMedium)
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            itemsIndexed(data.questions) { index, question ->
                val isCurrent = index == data.currentQuestionIndex
                val isAnswered = data.selectedAnswers.containsKey(question.id)
                val isPending = data.pendingMutations.containsKey(question.id)

                Card(
                    modifier = Modifier.padding(4.dp),
                    onClick = { onQuestionNavigate(index) },
                ) {
                    Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${index + 1}",
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isPending) {
                                CircularProgressIndicator(modifier = Modifier.width(12.dp))
                            } else if (isAnswered) {
                                Text("✓", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.padding(16.dp))

        // Current Question
        data.currentQuestion?.let { question ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Q${data.currentQuestionIndex + 1}: ${question.text}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.padding(8.dp))

                    question.options.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = data.selectedAnswers[question.id] == option.id,
                                onClick = { onOptionSelected(question.id, option.id) }
                            )
                            Text(text = option.text, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

package com.cglhustle.feature.activesession.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cglhustle.core.ui.state.UiState
import com.cglhustle.feature.activesession.ActiveSessionEvent
import com.cglhustle.feature.activesession.domain.ActiveSessionData
import com.cglhustle.feature.activesession.domain.Question
import com.cglhustle.feature.activesession.domain.SessionStatus
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun ActiveSessionScreen(
    uiState: UiState<ActiveSessionData>,
    events: SharedFlow<ActiveSessionEvent>,
    onOptionSelected: (String, String) -> Unit,
    onNavigateToQuestion: (Int) -> Unit,
    onTogglePause: () -> Unit,
    onSubmitSession: () -> Unit,
    onSessionComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is ActiveSessionEvent.SessionCompleted -> onSessionComplete(event.sessionId)
                is ActiveSessionEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is UiState.Loading -> CircularProgressIndicator()
                is UiState.Error -> Text("Error loading session.")
                is UiState.Success -> {
                    val data = uiState.data
                    ActiveSessionContent(
                        data = data,
                        onOptionSelected = onOptionSelected,
                        onNavigateToQuestion = onNavigateToQuestion,
                        onTogglePause = onTogglePause,
                        onSubmitSession = onSubmitSession
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveSessionContent(
    data: ActiveSessionData,
    onOptionSelected: (String, String) -> Unit,
    onNavigateToQuestion: (Int) -> Unit,
    onTogglePause: () -> Unit,
    onSubmitSession: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onTogglePause) {
                Text(if (data.status == SessionStatus.PAUSED) "Resume" else "Pause")
            }
            Button(
                onClick = onSubmitSession,
                enabled = data.status == SessionStatus.ACTIVE || data.status == SessionStatus.PAUSED
            ) {
                if (data.status == SessionStatus.SUBMITTING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit")
                }
            }
        }

        if (data.status == SessionStatus.PAUSED) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Session Paused", style = MaterialTheme.typography.headlineMedium)
            }
            return
        }

        if (data.status == SessionStatus.SUBMITTING) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircularProgressIndicator()
                    Text("Submitting to Server...", style = MaterialTheme.typography.headlineMedium)
                    Text("Please wait", style = MaterialTheme.typography.bodyLarge)
                }
            }
            return
        }

        // Current Question
        val currentQuestion = data.currentQuestion
        if (currentQuestion != null) {
            val isPending = data.pendingMutations.containsKey(currentQuestion.id)
            val selectedOptionId = data.selectedAnswers[currentQuestion.id]

            QuestionCard(
                question = currentQuestion,
                selectedOptionId = selectedOptionId,
                isPending = isPending,
                onOptionSelected = { optionId ->
                    onOptionSelected(currentQuestion.id, optionId)
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Navigation Grid
        Text("Questions", style = MaterialTheme.typography.titleMedium)
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(data.questions.size) { index ->
                val q = data.questions[index]
                val isAnswered = data.selectedAnswers.containsKey(q.id)
                val isCurrent = index == data.currentQuestionIndex

                Card(
                    modifier = Modifier
                        .height(48.dp)
                        .clickable { onNavigateToQuestion(index) },
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isCurrent -> MaterialTheme.colorScheme.primaryContainer
                            isAnswered -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text((index + 1).toString())
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionCard(
    question: Question,
    selectedOptionId: String?,
    isPending: Boolean,
    onOptionSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question.text,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                if (isPending) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }

            question.options.forEach { option ->
                val isSelected = option.id == selectedOptionId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isPending) { onOptionSelected(option.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = option.text,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}

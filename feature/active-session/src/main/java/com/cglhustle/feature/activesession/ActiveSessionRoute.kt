package com.cglhustle.feature.activesession

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cglhustle.feature.activesession.ui.ActiveSessionScreen

@Composable
fun ActiveSessionRoute(
    onSessionComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
    sessionId: String? = null,
    viewModel: ActiveSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(sessionId) {
        viewModel.initialize(sessionId)
    }

    ActiveSessionScreen(
        uiState = uiState,
        events = viewModel.events,
        onOptionSelected = viewModel::selectOption,
        onNavigateToQuestion = viewModel::navigateToQuestion,
        onTogglePause = viewModel::togglePause,
        onSubmitSession = viewModel::submitSession,
        onSessionComplete = onSessionComplete,
        modifier = modifier
    )
}

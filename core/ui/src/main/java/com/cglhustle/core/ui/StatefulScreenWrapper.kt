package com.cglhustle.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.NetworkError
import com.cglhustle.core.common.error.StorageError
import com.cglhustle.core.common.error.RecoveryAction

@Composable
fun <T> StatefulScreenWrapper(
    state: UiState<T>,
    onRetry: () -> Unit = {},
    content: @Composable (T) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (state) {
            is UiState.Loading -> {
                CircularProgressIndicator()
            }
            is UiState.Error -> {
                ErrorView(error = state.error, onRetry = onRetry)
            }
            is UiState.Success -> {
                content(state.data)
            }
        }
    }
}

@Composable
fun ErrorView(error: AppError, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "An error occurred: ${error.telemetryCode}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (error.recoveryAction == RecoveryAction.RETRY_SILENTLY ||
            error.recoveryAction == RecoveryAction.SHOW_TOAST) {
            Button(onClick = onRetry) {
                Text("Retry")
            }
        } else if (error.recoveryAction == RecoveryAction.FATAL_HALT) {
             Text(
                 text = "Fatal Error. Please restart the app.",
                 style = MaterialTheme.typography.bodyMedium,
                 color = MaterialTheme.colorScheme.error
             )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingPreview() {
    MaterialTheme {
        StatefulScreenWrapper<String>(state = UiState.Loading) {}
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorTransientPreview() {
    MaterialTheme {
        StatefulScreenWrapper<String>(
            state = UiState.Error(NetworkError.Transient()),
            onRetry = {}
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorFatalPreview() {
    MaterialTheme {
        StatefulScreenWrapper<String>(
            state = UiState.Error(StorageError.DatabaseCorruption()),
            onRetry = {}
        ) {}
    }
}

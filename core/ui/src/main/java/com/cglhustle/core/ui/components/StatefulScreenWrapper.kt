package com.cglhustle.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.NetworkError
import com.cglhustle.core.common.error.RecoveryAction
import com.cglhustle.core.ui.state.UiState

@Composable
fun <T> StatefulScreenWrapper(
    uiState: UiState<T>,
    onRetry: () -> Unit = {},
    content: @Composable (T) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is UiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is UiState.Error -> {
                ErrorView(
                    error = uiState.error,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is UiState.Success -> {
                content(uiState.data)
            }
        }
    }
}

@Composable
fun ErrorView(
    error: AppError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "An error occurred",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Code: ${error.telemetryCode}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (error.recoveryAction == RecoveryAction.RETRY_SILENTLY || error.recoveryAction == RecoveryAction.SHOW_TOAST) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatefulScreenWrapper_LoadingPreview() {
    StatefulScreenWrapper<String>(uiState = UiState.Loading) {
        Text("Content")
    }
}

@Preview(showBackground = true)
@Composable
fun StatefulScreenWrapper_SuccessPreview() {
    StatefulScreenWrapper(uiState = UiState.Success("Hello World")) { data ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(data)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatefulScreenWrapper_ErrorTransientPreview() {
    StatefulScreenWrapper<String>(
        uiState = UiState.Error(NetworkError.Transient())
    ) {
        Text("Content")
    }
}

@Preview(showBackground = true)
@Composable
fun StatefulScreenWrapper_ErrorFatalPreview() {
    StatefulScreenWrapper<String>(
        uiState = UiState.Error(com.cglhustle.core.common.error.StorageError.DatabaseCorruption())
    ) {
        Text("Content")
    }
}

package com.cglhustle.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.NetworkError
import com.cglhustle.core.common.error.RecoveryAction
import com.cglhustle.core.common.error.toUserFriendlyMessage
import com.cglhustle.core.ui.state.UiState

@Composable
fun <T> StatefulScreenWrapper(
    uiState: UiState<T>,
    onRetry: () -> Unit = {},
    loadingContent: @Composable () -> Unit = { DefaultLoadingSkeleton() },
    content: @Composable (T) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success && uiState.transientError != null) {
            snackbarHostState.showSnackbar(uiState.transientError.toUserFriendlyMessage())
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is UiState.Loading -> {
                loadingContent()
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

                if (uiState.transientError != null) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
fun DefaultLoadingSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        repeat(6) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (it == 0) 80.dp else 56.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            }
        }
        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
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
                text = error.toUserFriendlyMessage(),
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

@Composable
fun TransientErrorView(
    error: AppError,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = error.toUserFriendlyMessage(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
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

@Preview(showBackground = true)
@Composable
fun StatefulScreenWrapper_SuccessWithTransientPreview() {
    StatefulScreenWrapper(
        uiState = UiState.Success("Hello World", NetworkError.ServerOutage())
    ) { data ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(data)
        }
    }
}

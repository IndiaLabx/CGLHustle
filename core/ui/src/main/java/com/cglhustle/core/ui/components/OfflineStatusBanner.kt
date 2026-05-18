package com.cglhustle.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class SyncStatus {
    IDLE,
    IN_FLIGHT,
    FAILED_RETRY
}

@Composable
fun OfflineStatusBanner(
    isOffline: Boolean,
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier
) {
    if (!isOffline && syncStatus == SyncStatus.IDLE) {
        return
    }

    val backgroundColor = when {
        isOffline -> MaterialTheme.colorScheme.errorContainer
        syncStatus == SyncStatus.FAILED_RETRY -> MaterialTheme.colorScheme.errorContainer
        syncStatus == SyncStatus.IN_FLIGHT -> MaterialTheme.colorScheme.tertiaryContainer
        else -> Color.Transparent
    }

    val textColor = when {
        isOffline -> MaterialTheme.colorScheme.onErrorContainer
        syncStatus == SyncStatus.FAILED_RETRY -> MaterialTheme.colorScheme.onErrorContainer
        syncStatus == SyncStatus.IN_FLIGHT -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> Color.Transparent
    }

    val message = when {
        isOffline -> "You are offline. Changes will be saved locally."
        syncStatus == SyncStatus.FAILED_RETRY -> "Sync failed. Retrying in background..."
        syncStatus == SyncStatus.IN_FLIGHT -> "Syncing data..."
        else -> ""
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = textColor,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OfflineStatusBanner_Offline() {
    OfflineStatusBanner(
        isOffline = true,
        syncStatus = SyncStatus.IDLE
    )
}

@Preview(showBackground = true)
@Composable
fun OfflineStatusBanner_Syncing() {
    OfflineStatusBanner(
        isOffline = false,
        syncStatus = SyncStatus.IN_FLIGHT
    )
}

@Preview(showBackground = true)
@Composable
fun OfflineStatusBanner_FailedRetry() {
    OfflineStatusBanner(
        isOffline = false,
        syncStatus = SyncStatus.FAILED_RETRY
    )
}

package com.cglhustle.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    syncStatus: SyncStatus
) {
    if (isOffline) {
        BannerView(text = "You are currently offline.", backgroundColor = MaterialTheme.colorScheme.errorContainer)
    } else {
        when (syncStatus) {
            SyncStatus.IN_FLIGHT -> BannerView(text = "Syncing...", backgroundColor = MaterialTheme.colorScheme.tertiaryContainer)
            SyncStatus.FAILED_RETRY -> BannerView(text = "Sync failed. Retrying soon.", backgroundColor = MaterialTheme.colorScheme.errorContainer)
            SyncStatus.IDLE -> { /* No banner when idle and online */ }
        }
    }
}

@Composable
private fun BannerView(text: String, backgroundColor: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OfflineBannerPreview() {
    MaterialTheme {
        OfflineStatusBanner(isOffline = true, syncStatus = SyncStatus.IDLE)
    }
}

@Preview(showBackground = true)
@Composable
fun SyncInFlightBannerPreview() {
    MaterialTheme {
        OfflineStatusBanner(isOffline = false, syncStatus = SyncStatus.IN_FLIGHT)
    }
}

@Preview(showBackground = true)
@Composable
fun SyncFailedBannerPreview() {
    MaterialTheme {
        OfflineStatusBanner(isOffline = false, syncStatus = SyncStatus.FAILED_RETRY)
    }
}

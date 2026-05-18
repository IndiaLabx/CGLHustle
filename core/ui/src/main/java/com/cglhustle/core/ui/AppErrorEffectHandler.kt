package com.cglhustle.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.RecoveryAction

@Composable
fun AppErrorEffectHandler(
    error: AppError?,
    onNavigateToLogin: () -> Unit = {},
    onShowSnackbar: (String) -> Unit = {}
) {
    LaunchedEffect(error) {
        if (error != null) {
            when (error.recoveryAction) {
                RecoveryAction.PROMPT_LOGIN -> onNavigateToLogin()
                RecoveryAction.SHOW_TOAST -> onShowSnackbar(error.telemetryCode)
                RecoveryAction.RETRY_SILENTLY, RecoveryAction.FATAL_HALT -> { /* handled elsewhere or no-op */ }
            }
        }
    }
}

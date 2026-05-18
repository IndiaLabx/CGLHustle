package com.cglhustle.core.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.RecoveryAction

@Composable
fun AppErrorEffectHandler(
    error: AppError?,
    onNavigateToLogin: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    onFatalHalt: (AppError) -> Unit = {}
) {
    LaunchedEffect(error) {
        if (error != null) {
            when (error.recoveryAction) {
                RecoveryAction.PROMPT_LOGIN -> onNavigateToLogin()
                RecoveryAction.SHOW_TOAST -> onShowSnackbar(error.telemetryCode) // Or a mapped user-friendly message
                RecoveryAction.FATAL_HALT -> onFatalHalt(error)
                RecoveryAction.RETRY_SILENTLY -> {
                    // Handled automatically or by background processes, no immediate UI prompt needed
                }
            }
        }
    }
}

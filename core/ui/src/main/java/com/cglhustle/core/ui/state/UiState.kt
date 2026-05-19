package com.cglhustle.core.ui.state

import com.cglhustle.core.common.error.AppError

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<out T>(val data: T, val transientError: AppError? = null) : UiState<T>
    data class Error(val error: AppError) : UiState<Nothing>
}

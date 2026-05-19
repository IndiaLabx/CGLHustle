package com.cglhustle.feature.quizconfig.presentation

import com.cglhustle.feature.quizconfig.domain.model.QuizFilterOptions

sealed interface QuizConfigUiState {
    data object Loading : QuizConfigUiState
    data class Success(val filters: QuizFilterOptions) : QuizConfigUiState
    data class Error(val message: String) : QuizConfigUiState
}

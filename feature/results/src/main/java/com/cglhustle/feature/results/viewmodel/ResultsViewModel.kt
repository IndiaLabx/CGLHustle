package com.cglhustle.feature.results.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.AppResult
import com.cglhustle.core.common.error.Failure
import com.cglhustle.core.common.error.Success
import com.cglhustle.feature.results.data.remote.dto.AttemptedQuestionDto
import com.cglhustle.feature.results.data.remote.dto.BookmarkedQuestionDto
import com.cglhustle.feature.results.data.remote.dto.ResultsAnalyticsDto
import com.cglhustle.feature.results.domain.repository.ResultsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ResultsUiState<out T> {
    object Loading : ResultsUiState<Nothing>()
    data class Success<T>(val data: T) : ResultsUiState<T>()
    data class Error(val error: AppError) : ResultsUiState<Nothing>()
    object Empty : ResultsUiState<Nothing>()
}

@HiltViewModel
class ResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ResultsRepository
) : ViewModel() {

    private val sessionId: String = savedStateHandle.get<String>("sessionId") ?: "mock_results_001"

    private val _analyticsState = MutableStateFlow<ResultsUiState<ResultsAnalyticsDto>>(ResultsUiState.Loading)
    val analyticsState: StateFlow<ResultsUiState<ResultsAnalyticsDto>> = _analyticsState.asStateFlow()

    private val _attemptedState = MutableStateFlow<ResultsUiState<List<AttemptedQuestionDto>>>(ResultsUiState.Loading)
    val attemptedState: StateFlow<ResultsUiState<List<AttemptedQuestionDto>>> = _attemptedState.asStateFlow()

    private val _bookmarksState = MutableStateFlow<ResultsUiState<List<BookmarkedQuestionDto>>>(ResultsUiState.Loading)
    val bookmarksState: StateFlow<ResultsUiState<List<BookmarkedQuestionDto>>> = _bookmarksState.asStateFlow()

    init {
        // Initial fetch for the first tab
        fetchAnalytics()
    }

    fun fetchAnalytics() {
        viewModelScope.launch {
            _analyticsState.update { ResultsUiState.Loading }
            when (val result = repository.getAnalytics(sessionId)) {
                is Success -> _analyticsState.update { ResultsUiState.Success(result.data) }
                is Failure -> _analyticsState.update { ResultsUiState.Error(result.error) }
            }
        }
    }

    fun fetchAttemptedQuestions() {
        viewModelScope.launch {
            _attemptedState.update { ResultsUiState.Loading }
            when (val result = repository.getAttemptedQuestions(sessionId)) {
                is Success -> {
                    if (result.data.isEmpty()) {
                        _attemptedState.update { ResultsUiState.Empty }
                    } else {
                        _attemptedState.update { ResultsUiState.Success(result.data) }
                    }
                }
                is Failure -> _attemptedState.update { ResultsUiState.Error(result.error) }
            }
        }
    }

    fun fetchBookmarkedQuestions() {
        viewModelScope.launch {
            _bookmarksState.update { ResultsUiState.Loading }
            when (val result = repository.getBookmarkedQuestions(sessionId)) {
                is Success -> {
                    if (result.data.isEmpty()) {
                        _bookmarksState.update { ResultsUiState.Empty }
                    } else {
                        _bookmarksState.update { ResultsUiState.Success(result.data) }
                    }
                }
                is Failure -> _bookmarksState.update { ResultsUiState.Error(result.error) }
            }
        }
    }
}

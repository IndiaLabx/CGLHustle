package com.cglhustle.feature.quizconfig.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cglhustle.feature.quizconfig.domain.model.QuizConfigPayload
import com.cglhustle.feature.quizconfig.domain.repository.QuizConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizConfigViewModel @Inject constructor(
    private val repository: QuizConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizConfigUiState>(QuizConfigUiState.Loading)
    val uiState: StateFlow<QuizConfigUiState> = _uiState.asStateFlow()

    private val _isCreatingSession = MutableStateFlow(false)
    val isCreatingSession: StateFlow<Boolean> = _isCreatingSession.asStateFlow()

    private val _sessionCreatedEvent = MutableStateFlow<String?>(null)
    val sessionCreatedEvent: StateFlow<String?> = _sessionCreatedEvent.asStateFlow()

    init {
        loadFilters()
    }

    fun loadFilters() {
        viewModelScope.launch {
            _uiState.value = QuizConfigUiState.Loading
            try {
                val filters = repository.fetchAvailableFilters()
                _uiState.value = QuizConfigUiState.Success(filters)
            } catch (e: Exception) {
                _uiState.value = QuizConfigUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun startSession(payload: QuizConfigPayload) {
        viewModelScope.launch {
            _isCreatingSession.value = true
            try {
                val sessionId = repository.createSession(payload)
                _sessionCreatedEvent.value = sessionId
            } catch (e: Exception) {
                // For now, in case of error during creation, we could reset or show error.
                // Keeping it simple.
                _isCreatingSession.value = false
            }
        }
    }

    fun onSessionCreatedHandled() {
        _sessionCreatedEvent.value = null
        _isCreatingSession.value = false
    }
}

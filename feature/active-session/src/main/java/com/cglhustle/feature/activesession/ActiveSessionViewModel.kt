package com.cglhustle.feature.activesession

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cglhustle.core.ui.state.UiState
import com.cglhustle.feature.activesession.domain.ActiveSessionData
import com.cglhustle.feature.activesession.domain.ActiveSessionRepository
import com.cglhustle.feature.activesession.domain.PendingMutation
import com.cglhustle.feature.activesession.domain.SessionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed class ActiveSessionEvent {
    data class SessionCompleted(val sessionId: String) : ActiveSessionEvent()
    data class ShowSnackbar(val message: String) : ActiveSessionEvent()
}

@HiltViewModel
class ActiveSessionViewModel @Inject constructor(
    private val repository: ActiveSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ActiveSessionData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ActiveSessionData>> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ActiveSessionEvent>()
    val events = _events.asSharedFlow()

    private var initialized = false
    private var currentSessionId: String = ""
    private var userId: String = "mock_user_id" // Mock user id

    private var currentQuestions: List<com.cglhustle.feature.activesession.domain.Question> = emptyList()
    private var observeJob: Job? = null

    fun initialize(sessionId: String?) {
        if (initialized) return
        initialized = true

        currentSessionId = sessionId ?: "mock_active_session_${UUID.randomUUID().toString().take(8)}"

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                currentQuestions = repository.getQuestions(currentSessionId)

                observeJob?.cancel()
                observeJob = launch {
                    repository.observeSessionData(userId, currentSessionId).collectLatest { data ->
                        if (data != null) {
                            val currentIdx = (_uiState.value as? UiState.Success)?.data?.currentQuestionIndex ?: 0
                            val prevStatus = (_uiState.value as? UiState.Success)?.data?.status

                            _uiState.value = UiState.Success(
                                data.copy(
                                    questions = currentQuestions,
                                    currentQuestionIndex = currentIdx
                                )
                            )

                            // Fire completion event once it goes to COMPLETED
                            if (prevStatus == SessionStatus.SUBMITTING && data.status == SessionStatus.COMPLETED) {
                                _events.emit(ActiveSessionEvent.SessionCompleted(currentSessionId))
                            } else if (data.status == SessionStatus.COMPLETED && prevStatus != SessionStatus.COMPLETED) {
                                _events.emit(ActiveSessionEvent.SessionCompleted(currentSessionId))
                            }
                        } else {
                            // Empty state, or not started. For now we will keep it simple.
                             _uiState.value = UiState.Success(
                                ActiveSessionData(sessionId = currentSessionId, questions = currentQuestions)
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _events.emit(ActiveSessionEvent.ShowSnackbar("Failed to load session: ${e.message}"))
            }
        }
    }

    fun selectOption(questionId: String, optionId: String) {
        val currentState = _uiState.value as? UiState.Success ?: return
        val currentData = currentState.data

        if (currentData.status != SessionStatus.ACTIVE) return

        val eventId = UUID.randomUUID().toString().replace("-", "").take(26).uppercase()
        val idempotencyKey = UUID.randomUUID().toString()
        val attemptSequence = 1 // Simplified for this sprint

        viewModelScope.launch {
            try {
                repository.submitAnswer(
                    userId = userId,
                    sessionId = currentData.sessionId,
                    questionId = questionId,
                    eventId = eventId,
                    idempotencyKey = idempotencyKey,
                    selectedOptionId = optionId,
                    attemptSequence = attemptSequence
                )
            } catch (e: Exception) {
                _events.emit(ActiveSessionEvent.ShowSnackbar("Error saving answer locally."))
            }
        }
    }

    fun navigateToQuestion(index: Int) {
        _uiState.update { state ->
            if (state !is UiState.Success) return@update state
            if (index in state.data.questions.indices) {
                UiState.Success(state.data.copy(currentQuestionIndex = index))
            } else {
                state
            }
        }
    }

    fun togglePause() {
        val currentState = _uiState.value
        if (currentState is UiState.Success) {
            viewModelScope.launch {
                val isPaused = currentState.data.status == SessionStatus.PAUSED
                if (isPaused) {
                    repository.resumeSession(currentSessionId)
                } else {
                    repository.pauseSession(currentSessionId)
                }
            }
        }
    }

    fun submitSession() {
        val currentState = _uiState.value as? UiState.Success ?: return
        val currentData = currentState.data

        viewModelScope.launch {
            try {
                repository.submitSession(currentData.sessionId)
            } catch (e: Exception) {
                _events.emit(ActiveSessionEvent.ShowSnackbar("Failed to submit session locally."))
            }
        }
    }
}

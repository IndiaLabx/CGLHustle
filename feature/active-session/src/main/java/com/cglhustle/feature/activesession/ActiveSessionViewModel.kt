package com.cglhustle.feature.activesession

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cglhustle.core.network.dto.MutationStatus
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

    fun initialize(sessionId: String?) {
        if (initialized) return
        initialized = true

        currentSessionId = sessionId ?: "mock_active_session_${UUID.randomUUID().toString().take(8)}"

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val questions = repository.getQuestions(currentSessionId)
                _uiState.value = UiState.Success(
                    ActiveSessionData(
                        sessionId = currentSessionId,
                        questions = questions
                    )
                )
            } catch (e: Exception) {
                // Should use AppErrorMapper in real scenario
                _events.emit(ActiveSessionEvent.ShowSnackbar("Failed to load session: ${e.message}"))
            }
        }
    }

    fun selectOption(questionId: String, optionId: String) {
        val currentState = _uiState.value as? UiState.Success ?: return
        val currentData = currentState.data

        if (currentData.status != SessionStatus.ACTIVE) return

        val eventId = UUID.randomUUID().toString()

        // Optimistic Update
        val newPendingMutation = PendingMutation(questionId, optionId, eventId)
        val updatedAnswers = currentData.selectedAnswers + (questionId to optionId)
        val updatedPending = currentData.pendingMutations + (questionId to newPendingMutation)

        _uiState.update {
            UiState.Success(
                currentData.copy(
                    selectedAnswers = updatedAnswers,
                    pendingMutations = updatedPending
                )
            )
        }

        // Fire request to server
        viewModelScope.launch {
            val result = repository.submitAnswer(
                sessionId = currentData.sessionId,
                questionId = questionId,
                optionId = optionId,
                eventId = eventId
            )

            result.onSuccess { response ->
                reconcileMutation(questionId, eventId, response.status)
            }.onFailure { error ->
                // Treat network failure similar to conflict for now, or just show error and clear pending
                reconcileMutation(questionId, eventId, MutationStatus.CONFLICT)
                _events.emit(ActiveSessionEvent.ShowSnackbar("Network error submitting answer. Please try again."))
            }
        }
    }

    private fun reconcileMutation(questionId: String, eventId: String, status: MutationStatus) {
        _uiState.update { state ->
            if (state !is UiState.Success) return@update state
            val data = state.data

            val pending = data.pendingMutations[questionId]

            // If the pending mutation eventId doesn't match, it means a newer mutation is in flight.
            // We ignore this old response.
            if (pending?.eventId != eventId) return@update state

            val updatedPending = data.pendingMutations - questionId

            if (status == MutationStatus.CONFLICT) {
                // Revert the answer on conflict
                val updatedAnswers = data.selectedAnswers - questionId
                UiState.Success(
                    data.copy(
                        selectedAnswers = updatedAnswers,
                        pendingMutations = updatedPending
                    )
                )
            } else {
                // APPLIED or NOOP: Keep the answer, just clear pending status
                UiState.Success(
                    data.copy(
                        pendingMutations = updatedPending
                    )
                )
            }
        }

        if (status == MutationStatus.CONFLICT) {
            viewModelScope.launch {
                _events.emit(ActiveSessionEvent.ShowSnackbar("Conflict detected. Server state restored."))
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
        _uiState.update { state ->
            if (state !is UiState.Success) return@update state
            val newStatus = if (state.data.status == SessionStatus.ACTIVE) SessionStatus.PAUSED else SessionStatus.ACTIVE
            UiState.Success(state.data.copy(status = newStatus))
        }
    }

    fun submitSession() {
        val currentState = _uiState.value as? UiState.Success ?: return
        val currentData = currentState.data

        _uiState.update {
            UiState.Success(currentData.copy(status = SessionStatus.SUBMITTING))
        }

        viewModelScope.launch {
            val result = repository.submitSession(currentData.sessionId)
            result.onSuccess {
                _uiState.update { state ->
                    if (state is UiState.Success) {
                        UiState.Success(state.data.copy(status = SessionStatus.COMPLETED))
                    } else state
                }
                _events.emit(ActiveSessionEvent.SessionCompleted(currentData.sessionId))
            }.onFailure { error ->
                _uiState.update { state ->
                    if (state is UiState.Success) {
                        UiState.Success(state.data.copy(status = SessionStatus.ACTIVE))
                    } else state
                }
                _events.emit(ActiveSessionEvent.ShowSnackbar("Failed to submit session. Try again."))
            }
        }
    }
}

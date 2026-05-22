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
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import com.cglhustle.core.network.auth.AuthRepository
import io.github.jan.supabase.gotrue.SessionStatus as SupabaseSessionStatus

sealed class ActiveSessionEvent {
    data class SessionCompleted(val sessionId: String) : ActiveSessionEvent()
    data class ShowSnackbar(val message: String) : ActiveSessionEvent()
}

@HiltViewModel
class ActiveSessionViewModel @Inject constructor(
    private val repository: ActiveSessionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ActiveSessionData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ActiveSessionData>> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ActiveSessionEvent>()
    val events = _events.asSharedFlow()

    private var initialized = false
    private var currentSessionId: String = ""
    private val userId: String
        get() {
            val session = authRepository.sessionStatus.value
            return if (session is SupabaseSessionStatus.Authenticated) session.session.user?.id ?: "" else ""
        }

    fun initialize(sessionId: String?) {
        if (initialized) return
        initialized = true

        currentSessionId = sessionId ?: "mock_active_session_${UUID.randomUUID().toString().take(8)}"

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // 1. Fetch Questions
                val questions = repository.getQuestions(currentSessionId)

                // 2. Hydrate session data
                val initialData = repository.getInitialSessionData(userId, currentSessionId)

                if (initialData != null) {
                    _uiState.value = UiState.Success(
                        initialData.copy(questions = questions)
                    )
                } else {
                    _uiState.value = UiState.Success(
                        ActiveSessionData(sessionId = currentSessionId, questions = questions)
                    )
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

        // 1. Optimistic In-Memory State Update
        val newSelectedAnswers = currentData.selectedAnswers.toMutableMap()
        newSelectedAnswers[questionId] = optionId

        val newPendingMutations = currentData.pendingMutations.toMutableMap()
        newPendingMutations[questionId] = PendingMutation(questionId, optionId, eventId)

        _uiState.value = UiState.Success(
            currentData.copy(
                selectedAnswers = newSelectedAnswers,
                pendingMutations = newPendingMutations
            )
        )

        // 2. Fire and forget repository execution (direct network + fallback)
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

                // 3. Clear pending mutation on success (or let the background process handle it, but for UI, we can clear the spinner)
                _uiState.update { state ->
                    if (state is UiState.Success) {
                        val currentPending = state.data.pendingMutations.toMutableMap()
                        if (currentPending[questionId]?.eventId == eventId) {
                            currentPending.remove(questionId)
                            UiState.Success(state.data.copy(pendingMutations = currentPending))
                        } else {
                            state
                        }
                    } else state
                }

            } catch (e: Exception) {
                _events.emit(ActiveSessionEvent.ShowSnackbar("Answer submission error. Retrying in background."))

                // Even on error, we clear the loading spinner because the retry engine will take over
                _uiState.update { state ->
                    if (state is UiState.Success) {
                        val currentPending = state.data.pendingMutations.toMutableMap()
                        if (currentPending[questionId]?.eventId == eventId) {
                            currentPending.remove(questionId)
                            UiState.Success(state.data.copy(pendingMutations = currentPending))
                        } else {
                            state
                        }
                    } else state
                }
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
        val currentState = _uiState.value as? UiState.Success ?: return
        val currentData = currentState.data

        val isPaused = currentData.status == SessionStatus.PAUSED
        val newStatus = if (isPaused) SessionStatus.ACTIVE else SessionStatus.PAUSED

        // Optimistic
        _uiState.value = UiState.Success(currentData.copy(status = newStatus))

        viewModelScope.launch {
            try {
                if (isPaused) {
                    repository.resumeSession(currentSessionId)
                } else {
                    repository.pauseSession(currentSessionId)
                }
            } catch (e: Exception) {
                // Revert optimistic on failure
                _uiState.value = UiState.Success(currentData.copy(status = currentData.status))
                _events.emit(ActiveSessionEvent.ShowSnackbar("Failed to update pause state."))
            }
        }
    }

    fun submitSession() {
        val currentState = _uiState.value as? UiState.Success ?: return
        val currentData = currentState.data

        // Optimistic
        _uiState.value = UiState.Success(currentData.copy(status = SessionStatus.SUBMITTING))

        viewModelScope.launch {
            try {
                repository.submitSession(currentData.sessionId)
                _uiState.value = UiState.Success(currentData.copy(status = SessionStatus.COMPLETED))
                _events.emit(ActiveSessionEvent.SessionCompleted(currentSessionId))
            } catch (e: Exception) {
                // Revert optimistic
                _uiState.value = UiState.Success(currentData.copy(status = currentData.status))
                _events.emit(ActiveSessionEvent.ShowSnackbar("Failed to submit session."))
            }
        }
    }
}

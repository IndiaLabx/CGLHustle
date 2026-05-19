package com.cglhustle.feature.activesession.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cglhustle.core.common.error.NetworkError
import com.cglhustle.core.network.dto.AnswerMutationRequest
import com.cglhustle.core.network.dto.MutationStatus
import com.cglhustle.core.ui.state.UiState
import com.cglhustle.feature.activesession.domain.ActiveSessionRepository
import com.cglhustle.feature.activesession.domain.Question
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class PendingMutation(
    val questionId: String,
    val selectedOptionId: String,
    val idempotencyKey: String
)

data class ActiveSessionData(
    val sessionId: String,
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val answers: Map<String, String> = emptyMap(),
    val pendingMutations: List<PendingMutation> = emptyList(),
    val isPaused: Boolean = false,
    val isSubmitting: Boolean = false
) {
    val currentQuestion: Question?
        get() = questions.getOrNull(currentQuestionIndex)
}

@HiltViewModel
class ActiveSessionViewModel @Inject constructor(
    private val repository: ActiveSessionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ActiveSessionData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ActiveSessionData>> = _uiState.asStateFlow()

    private var sessionId: String = ""
    private var userId: String = "mock_user_id"

    fun initialize(passedSessionId: String?) {
        this.sessionId = passedSessionId ?: "mock_active_session_001"
        loadSessionData()
    }

    private fun loadSessionData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val questions = repository.getQuestions(sessionId)
                _uiState.value = UiState.Success(
                    ActiveSessionData(
                        sessionId = sessionId,
                        questions = questions
                    )
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(NetworkError.Transient(e.message ?: "Unknown error"))
            }
        }
    }

    fun selectOption(questionId: String, optionId: String) {
        val currentState = _uiState.value
        if (currentState is UiState.Success) {
            val idempotencyKey = UUID.randomUUID().toString()
            val pendingMutation = PendingMutation(questionId, optionId, idempotencyKey)

            val updatedData = currentState.data.copy(
                answers = currentState.data.answers + (questionId to optionId),
                pendingMutations = currentState.data.pendingMutations + pendingMutation
            )
            _uiState.value = UiState.Success(updatedData)

            submitMutation(questionId, optionId, idempotencyKey)
        }
    }

    private fun submitMutation(questionId: String, optionId: String, idempotencyKey: String) {
        viewModelScope.launch {
            try {
                val request = AnswerMutationRequest(
                    userId = userId,
                    sessionId = sessionId,
                    questionId = questionId,
                    eventId = UUID.randomUUID().toString().replace("-", "").take(26).uppercase(),
                    idempotencyKey = idempotencyKey
                )

                val response = repository.submitAnswer(request)

                val currentState = _uiState.value
                if (currentState is UiState.Success) {
                    val updatedMutations = currentState.data.pendingMutations.filterNot { it.idempotencyKey == idempotencyKey }

                    if (response.status == MutationStatus.CONFLICT) {
                        val updatedAnswers = currentState.data.answers.toMutableMap()
                        if (updatedAnswers[questionId] == optionId) {
                            updatedAnswers.remove(questionId)
                        }
                        _uiState.value = UiState.Success(
                            currentState.data.copy(
                                answers = updatedAnswers,
                                pendingMutations = updatedMutations
                            )
                        )
                    } else {
                        _uiState.value = UiState.Success(
                            currentState.data.copy(pendingMutations = updatedMutations)
                        )
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    fun goToQuestion(index: Int) {
        val currentState = _uiState.value
        if (currentState is UiState.Success) {
            if (index in currentState.data.questions.indices) {
                _uiState.value = UiState.Success(
                    currentState.data.copy(currentQuestionIndex = index)
                )
            }
        }
    }

    fun togglePause() {
        val currentState = _uiState.value
        if (currentState is UiState.Success) {
            viewModelScope.launch {
                val isPaused = currentState.data.isPaused
                val updatedData = currentState.data.copy(isPaused = !isPaused)
                _uiState.value = UiState.Success(updatedData)

                if (isPaused) {
                    repository.resumeSession(sessionId)
                } else {
                    repository.pauseSession(sessionId)
                }
            }
        }
    }

    fun submitSession(onComplete: (String) -> Unit) {
        val currentState = _uiState.value
        if (currentState is UiState.Success) {
            viewModelScope.launch {
                _uiState.value = UiState.Success(
                    currentState.data.copy(isSubmitting = true)
                )

                try {
                    repository.submitSession(sessionId)
                    onComplete(sessionId)
                } catch (e: Exception) {
                     _uiState.value = UiState.Success(
                        currentState.data.copy(isSubmitting = false)
                    )
                }
            }
        }
    }
}

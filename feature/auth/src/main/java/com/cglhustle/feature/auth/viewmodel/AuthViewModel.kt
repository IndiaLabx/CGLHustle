package com.cglhustle.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cglhustle.core.network.auth.AuthRepository
import com.cglhustle.feature.auth.state.AuthUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onTabSwitched(isSignUp: Boolean) {
        _uiState.update { it.copy(isSignUpMode = isSignUp, errorMessage = null, successMessage = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(emailInput = email) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(passwordInput = password) }
    }

    fun onConfirmPasswordChanged(password: String) {
        _uiState.update { it.copy(confirmPasswordInput = password) }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(nameInput = name) }
    }

    fun onAgeCheckedChanged(isChecked: Boolean) {
        _uiState.update { it.copy(isAgeChecked = isChecked) }
    }

    fun onPrivacyCheckedChanged(isChecked: Boolean) {
        _uiState.update { it.copy(isPrivacyChecked = isChecked) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun signInWithGoogle(idToken: String) {
        _uiState.update { it.copy(isGoogleLoading = true, errorMessage = null) }
        viewModelScope.launch {
            authRepository.signInWithGoogle(idToken).fold(
                onSuccess = {
                    _uiState.update { it.copy(isGoogleLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isGoogleLoading = false, errorMessage = error.message ?: "Google Sign-In failed") }
                }
            )
        }
    }

    fun signInWithGuest() {
        _uiState.update { it.copy(isGuestLoading = true, errorMessage = null) }
        viewModelScope.launch {
            authRepository.signInWithEmail("cglhustle@user.com", "Test@%£8167").fold(
                onSuccess = {
                    _uiState.update { it.copy(isGuestLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isGuestLoading = false, errorMessage = error.message ?: "Guest login failed") }
                }
            )
        }
    }

    fun submitEmailForm() {
        val currentState = _uiState.value
        _uiState.update { it.copy(isEmailLoading = true, errorMessage = null) }

        viewModelScope.launch {
            if (currentState.isSignUpMode) {
                // Validation
                if (currentState.passwordInput != currentState.confirmPasswordInput) {
                    _uiState.update { it.copy(isEmailLoading = false, errorMessage = "Passwords do not match") }
                    return@launch
                }
                if (!currentState.isAgeChecked || !currentState.isPrivacyChecked) {
                    _uiState.update { it.copy(isEmailLoading = false, errorMessage = "Please accept the terms and policy") }
                    return@launch
                }
                if (currentState.emailInput.isBlank() || currentState.passwordInput.isBlank() || currentState.nameInput.isBlank()) {
                     _uiState.update { it.copy(isEmailLoading = false, errorMessage = "All fields are required") }
                     return@launch
                }

                authRepository.signUpWithEmail(currentState.emailInput, currentState.passwordInput, currentState.nameInput).fold(
                    onSuccess = {
                        _uiState.update { it.copy(
                            isEmailLoading = false,
                            successMessage = "Check your email for the confirmation link!",
                            isSignUpMode = false // switch to login implicitly
                        ) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isEmailLoading = false, errorMessage = error.message ?: "Sign up failed") }
                    }
                )
            } else {
                if (currentState.emailInput.isBlank() || currentState.passwordInput.isBlank()) {
                     _uiState.update { it.copy(isEmailLoading = false, errorMessage = "Email and Password required") }
                     return@launch
                }
                authRepository.signInWithEmail(currentState.emailInput, currentState.passwordInput).fold(
                    onSuccess = {
                        _uiState.update { it.copy(isEmailLoading = false) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isEmailLoading = false, errorMessage = error.message ?: "Invalid credentials") }
                    }
                )
            }
        }
    }
}

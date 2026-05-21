package com.cglhustle.feature.auth.state

data class AuthUiState(
    val isSignUpMode: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val isEmailLoading: Boolean = false,
    val isGuestLoading: Boolean = false,
    val emailInput: String = "",
    val passwordInput: String = "",
    val confirmPasswordInput: String = "",
    val nameInput: String = "",
    val isAgeChecked: Boolean = false,
    val isPrivacyChecked: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

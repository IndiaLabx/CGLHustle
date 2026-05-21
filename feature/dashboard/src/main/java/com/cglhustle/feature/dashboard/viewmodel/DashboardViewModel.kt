package com.cglhustle.feature.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class DashboardUiState(
    val userName: String = "",
    val email: String = ""
)

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState(userName = "", email = "user@example.com"))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun isAdmin(): Boolean {
        return _uiState.value.email == "admin@mindflow.com"
    }
}

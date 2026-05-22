package com.cglhustle.feature.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.cglhustle.core.network.auth.AuthRepository
import io.github.jan.supabase.gotrue.SessionStatus
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class DashboardUiState(
    val userName: String = "",
    val email: String = ""
)



@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.sessionStatus.collectLatest { status ->
                if (status is SessionStatus.Authenticated) {
                    val user = status.session.user
                    val email = user?.email ?: ""
                    val name = user?.userMetadata?.get("name")?.toString()?.replace("\"", "") ?: ""
                    _uiState.value = DashboardUiState(userName = name, email = email)
                } else {
                    _uiState.value = DashboardUiState()
                }
            }
        }
    }


    fun isAdmin(): Boolean {
        return _uiState.value.email == "admin@mindflow.com"
    }
}

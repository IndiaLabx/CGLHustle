package com.cglhustle.feature.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cglhustle.core.network.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.gotrue.SessionStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val userName: String = "",
    val email: String = ""
)

sealed interface DashboardEvent {
    data class NavigateTo(val route: String) : DashboardEvent
    data class OpenExternalLink(val url: String) : DashboardEvent
    data class ShowToast(val message: String) : DashboardEvent
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DashboardEvent>()
    val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()

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
        return _uiState.value.email == "admin@cglhustle.com"
    }

    fun onActionClick(route: String?) {
        viewModelScope.launch {
            if (route == null) {
                // Currently only the download action has no route
                _events.emit(DashboardEvent.OpenExternalLink("https://drive.google.com/drive/folders/1Owy8_qnvMOTw5WLRGLQajCiScN-dOHtF"))
                _events.emit(DashboardEvent.ShowToast("Your download page has been opened."))
            } else {
                _events.emit(DashboardEvent.NavigateTo(route))
            }
        }
    }
}

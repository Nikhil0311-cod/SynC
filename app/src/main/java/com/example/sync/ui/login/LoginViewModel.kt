package com.example.sync.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sync.data.local.AuthManager
import com.example.sync.data.model.LoginRequest
import com.example.sync.data.repository.CampusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val error: String? = null
)

class LoginViewModel(
    private val repository: CampusRepository,
    private val authManager: AuthManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(usernameOrEmail: String, password: String) {
        if (usernameOrEmail.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Please fill in all fields") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            repository.login(LoginRequest(email = usernameOrEmail, password = password)).collect { result ->
                result.onSuccess { authResponse ->
                    try {
                        authManager.saveAuthData(
                            token = authResponse.token,
                            id = authResponse.user.id,
                            name = authResponse.user.name,
                            role = authResponse.user.role
                        )
                        _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(isLoading = false, error = "Failed to save session: ${e.message}") }
                    }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Login failed") }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun onLoginSuccessHandled() {
        _uiState.update { it.copy(loginSuccess = false) }
    }
}

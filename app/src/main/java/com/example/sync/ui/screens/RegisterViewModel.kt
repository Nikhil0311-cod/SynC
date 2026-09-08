package com.example.sync.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sync.data.model.RegisterRequest
import com.example.sync.data.repository.CampusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val registerSuccess: Boolean = false,
    val error: String? = null
)

class RegisterViewModel(
    private val repository: CampusRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _uiState.update { it.copy(error = "Please fill in all fields") }
            return
        }

        if (password != confirmPassword) {
            _uiState.update { it.copy(error = "Passwords do not match") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            repository.register(RegisterRequest(name, email, password)).collect { result ->
                result.onSuccess {
                    _uiState.update { it.copy(isLoading = false, registerSuccess = true) }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Registration failed") }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun onRegisterSuccessHandled() {
        _uiState.update { it.copy(registerSuccess = false) }
    }
}

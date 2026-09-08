package com.example.sync.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sync.data.local.AuthManager
import com.example.sync.data.repository.CampusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class SplashNavigationState {
    object Loading : SplashNavigationState()
    object Authenticated : SplashNavigationState()
    object Unauthenticated : SplashNavigationState()
}

class SplashViewModel(
    private val repository: CampusRepository,
    private val authManager: AuthManager
) : ViewModel() {
    private val _navState = MutableStateFlow<SplashNavigationState>(SplashNavigationState.Loading)
    val navState: StateFlow<SplashNavigationState> = _navState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val token = authManager.authToken.first()
            if (token.isNullOrBlank()) {
                _navState.value = SplashNavigationState.Unauthenticated
                return@launch
            }

            repository.getCurrentUser().collect { result ->
                if (result.isSuccess) {
                    _navState.value = SplashNavigationState.Authenticated
                } else {
                    // If 401, clear local data and go to login
                    authManager.clearAuthData()
                    _navState.value = SplashNavigationState.Unauthenticated
                }
            }
        }
    }
}

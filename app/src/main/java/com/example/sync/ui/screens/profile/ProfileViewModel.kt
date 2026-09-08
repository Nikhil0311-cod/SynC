package com.example.sync.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sync.data.model.UpdateProfileRequest
import com.example.sync.data.model.UserDto
import com.example.sync.data.repository.CampusRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: UserDto? = null,
    val pollsCount: Int = 0,
    val itemsPosted: Int = 0,
    val error: String? = null
)

class ProfileViewModel(
    private val repository: CampusRepository,
    private val authManager: com.example.sync.data.local.AuthManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            combine(
                repository.getCurrentUser(),
                repository.getPolls(),
                repository.getLostFound()
            ) { profile, polls, items ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = profile.getOrNull(),
                        pollsCount = polls.getOrNull()?.size ?: 0,
                        itemsPosted = items.getOrNull()?.size ?: 0,
                        error = profile.exceptionOrNull()?.message
                    )
                }
            }.collect()
        }
    }

    fun logout() {
        viewModelScope.launch {
            authManager.clearAuthData()
        }
    }

    fun updateProfile(name: String, avatarUrl: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.updateProfile(UpdateProfileRequest(name, avatarUrl)).collect { result ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = result.getOrNull() ?: it.user,
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    fun uploadAvatar(imagePart: okhttp3.MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.uploadAvatar(imagePart).collect { result ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = result.getOrNull() ?: it.user,
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
}

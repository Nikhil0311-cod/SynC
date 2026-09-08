package com.example.sync.ui.screens.polls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sync.data.local.AuthManager
import com.example.sync.data.model.CreatePollRequest
import com.example.sync.data.model.PollDto
import com.example.sync.data.repository.CampusRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PollsUiState(
    val isLoading: Boolean = false,
    val polls: List<PollDto> = emptyList(),
    val isAdmin: Boolean = false,
    val error: String? = null
)

class PollsViewModel(
    private val repository: CampusRepository,
    private val authManager: AuthManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(PollsUiState())
    val uiState: StateFlow<PollsUiState> = _uiState.asStateFlow()

    init {
        loadPolls()
        observeUserRole()
    }

    private fun observeUserRole() {
        viewModelScope.launch {
            authManager.userRole.collect { role ->
                _uiState.update { it.copy(isAdmin = role == "admin") }
            }
        }
    }

    fun refresh() {
        loadPolls()
    }

    private fun loadPolls() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getPolls().collect { result ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        polls = result.getOrNull() ?: emptyList(),
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    fun vote(pollId: Int, optionIndex: Int) {
        viewModelScope.launch {
            repository.votePoll(pollId, optionIndex).collect { result ->
                if (result.isSuccess) {
                    loadPolls() // Refresh to get updated counts from server
                }
            }
        }
    }

    fun createPoll(question: String, options: List<String>) {
        viewModelScope.launch {
            val userId = authManager.userId.first()
            if (userId == -1) return@launch

            repository.createPoll(CreatePollRequest(question, options, userId)).collect { result ->
                if (result.isSuccess) refresh()
            }
        }
    }

    fun deletePoll(id: Int) {
        viewModelScope.launch {
            repository.deletePoll(id).collect { result ->
                if (result.isSuccess) refresh()
            }
        }
    }
}

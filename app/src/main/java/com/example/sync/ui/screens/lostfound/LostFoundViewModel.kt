package com.example.sync.ui.screens.lostfound

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sync.data.local.AuthManager
import com.example.sync.data.model.CreateLostFoundRequest
import com.example.sync.data.model.LostFoundDto
import com.example.sync.data.repository.CampusRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LostFoundUiState(
    val isLoading: Boolean = false,
    val items: List<LostFoundDto> = emptyList(),
    val searchQuery: String = "",
    val isAdmin: Boolean = false,
    val error: String? = null
)

class LostFoundViewModel(
    private val repository: CampusRepository,
    private val authManager: AuthManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(LostFoundUiState())
    val uiState: StateFlow<LostFoundUiState> = _uiState.asStateFlow()

    init {
        loadItems()
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
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getLostFound().collect { result ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = result.getOrNull() ?: emptyList(),
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun deleteItem(itemId: Int) {
        viewModelScope.launch {
            repository.deleteLostFound(itemId).collect { result ->
                if (result.isSuccess) {
                    loadItems()
                }
            }
        }
    }

    fun postItem(name: String, status: String, location: String, description: String) {
        viewModelScope.launch {
            val userId = authManager.userId.first()
            if (userId == -1) return@launch
            
            val request = CreateLostFoundRequest(
                itemName = name,
                status = status,
                location = location,
                description = description,
                userId = userId
            )
            repository.createLostFound(request).collect { result ->
                if (result.isSuccess) {
                    loadItems()
                }
            }
        }
    }
}

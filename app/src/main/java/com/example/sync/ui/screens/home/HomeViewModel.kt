package com.example.sync.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sync.data.local.AuthManager
import com.example.sync.data.model.*
import com.example.sync.data.repository.CampusRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Represents the UI state for the Home/Dashboard screen.
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val user: UserDto? = null,
    val activePoll: PollDto? = null,
    val lostItemsCount: Int = 0,
    val announcements: List<AnnouncementDto> = emptyList(),
    val isAdmin: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Home screen.
 * Aggregates data from multiple repository calls to provide a unified dashboard view.
 */
class HomeViewModel(
    private val repository: CampusRepository,
    private val authManager: AuthManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        observeUserRole()
    }

    /**
     * Observes the user's role from local storage to enable/disable admin features in the UI.
     */
    private fun observeUserRole() {
        viewModelScope.launch {
            authManager.userRole.collect { role ->
                _uiState.update { it.copy(isAdmin = role == "admin") }
            }
        }
    }

    /**
     * Manual refresh trigger for the dashboard data.
     */
    fun refresh() {
        loadDashboardData()
    }

    /**
     * Orchestrates multiple network requests (Profile, Polls, Lost & Found, Announcements)
     * and combines them into a single [HomeUiState].
     */
    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            combine(
                repository.getCurrentUser(),
                repository.getPolls(),
                repository.getLostFound(),
                repository.getAnnouncements()
            ) { profile, polls, lostItems, announcements ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = profile.getOrNull(),
                        activePoll = polls.getOrNull()?.firstOrNull(),
                        lostItemsCount = lostItems.getOrNull()?.count { item -> item.status == "lost" } ?: 0,
                        announcements = announcements.getOrNull() ?: emptyList(),
                        error = if (profile.isFailure) "Failed to load dashboard"
                                else if (announcements.isFailure) "Failed to load feed"
                                else null
                    )
                }
            }.collect()
        }
    }

    /**
     * Creates a new announcement. Requires the current user's ID for attribution.
     */
    fun createAnnouncement(title: String, category: String, body: String) {
        viewModelScope.launch {
            val userId = authManager.userId.first()
            if (userId == -1) return@launch

            repository.createAnnouncement(CreateAnnouncementRequest(title, category, body, userId)).collect { result ->
                if (result.isSuccess) refresh()
            }
        }
    }

    /**
     * Deletes an announcement (Admin only functionality).
     */
    fun deleteAnnouncement(id: Int) {
        viewModelScope.launch {
            repository.deleteAnnouncement(id).collect { result ->
                if (result.isSuccess) refresh()
            }
        }
    }
}

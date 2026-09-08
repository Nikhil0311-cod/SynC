package com.example.sync.ui.screens.profile

import com.example.sync.data.local.AuthManager
import com.example.sync.data.model.UserDto
import com.example.sync.data.repository.CampusRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private lateinit var viewModel: ProfileViewModel
    private val repository = mockk<CampusRepository>()
    private val authManager = mockk<AuthManager>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock default behavior for init blocks
        coEvery { repository.getCurrentUser() } returns flowOf(Result.success(UserDto(1, "Test", "t@e.com", null, "student", null)))
        coEvery { repository.getPolls() } returns flowOf(Result.success(emptyList()))
        coEvery { repository.getLostFound() } returns flowOf(Result.success(emptyList()))
        
        viewModel = ProfileViewModel(repository, authManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProfile success updates uiState`() = runTest {
        val user = UserDto(1, "Test User", "test@example.com", "url", "student", "date")
        
        coEvery { repository.getCurrentUser() } returns flowOf(Result.success(user))
        
        // Re-init to trigger loadProfile with new mock
        viewModel = ProfileViewModel(repository, authManager)

        assertEquals(user, viewModel.uiState.value.user)
        assertEquals(0, viewModel.uiState.value.pollsCount)
    }

    @Test
    fun `logout calls authManager clear`() = runTest {
        viewModel.logout()
        coVerify { authManager.clearAuthData() }
    }
}

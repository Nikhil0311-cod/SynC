package com.example.sync.ui.login

import com.example.sync.data.local.AuthManager
import com.example.sync.data.model.AuthResponseDto
import com.example.sync.data.model.LoginRequest
import com.example.sync.data.model.UserDto
import com.example.sync.data.repository.CampusRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private lateinit var viewModel: LoginViewModel
    private val repository = mockk<CampusRepository>()
    private val authManager = mockk<AuthManager>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(repository, authManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login success updates uiState`() = runTest {
        val user = UserDto(1, "Test User", "test@example.com", null, "student", null)
        val authResponse = AuthResponseDto("token123", user)
        
        coEvery { repository.login(any()) } returns flowOf(Result.success(authResponse))

        viewModel.login("test@example.com", "password")

        assertTrue(viewModel.uiState.value.loginSuccess)
        assertEquals(null, viewModel.uiState.value.error)
        
        coVerify { authManager.saveAuthData("token123", 1, "Test User", "student") }
    }

    @Test
    fun `login failure updates uiState with error`() = runTest {
        coEvery { repository.login(any()) } returns flowOf(Result.failure(Exception("Login failed")))

        viewModel.login("test@example.com", "password")

        assertTrue(!viewModel.uiState.value.loginSuccess)
        assertEquals("Login failed", viewModel.uiState.value.error)
    }
}

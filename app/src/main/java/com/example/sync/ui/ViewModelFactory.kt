package com.example.sync.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sync.data.repository.CampusRepository
import com.example.sync.ui.login.LoginViewModel
import com.example.sync.ui.screens.RegisterViewModel
import com.example.sync.ui.screens.SplashViewModel
import com.example.sync.ui.screens.home.HomeViewModel
import com.example.sync.ui.screens.lostfound.LostFoundViewModel
import com.example.sync.ui.screens.materials.MaterialsViewModel
import com.example.sync.ui.screens.polls.PollsViewModel
import com.example.sync.ui.screens.profile.ProfileViewModel

class ViewModelFactory(
    private val repository: CampusRepository,
    private val authManager: com.example.sync.data.local.AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(repository, authManager) as T
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> RegisterViewModel(repository) as T
            modelClass.isAssignableFrom(SplashViewModel::class.java) -> SplashViewModel(repository, authManager) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository, authManager) as T
            modelClass.isAssignableFrom(PollsViewModel::class.java) -> PollsViewModel(repository, authManager) as T
            modelClass.isAssignableFrom(LostFoundViewModel::class.java) -> LostFoundViewModel(repository, authManager) as T
            modelClass.isAssignableFrom(MaterialsViewModel::class.java) -> MaterialsViewModel(repository, authManager) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(repository, authManager) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

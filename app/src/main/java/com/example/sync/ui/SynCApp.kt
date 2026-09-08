package com.example.sync.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sync.data.local.AuthManager
import com.example.sync.data.remote.RetrofitClient
import com.example.sync.data.repository.CampusRepository
import com.example.sync.ui.components.GlassBottomNavigation
import com.example.sync.ui.login.LoginScreen
import com.example.sync.ui.login.LoginViewModel
import com.example.sync.ui.screens.RegisterViewModel
import com.example.sync.ui.screens.RegisterScreen
import com.example.sync.ui.screens.SplashViewModel
import com.example.sync.ui.screens.SplashScreen
import com.example.sync.ui.screens.home.HomeScreen
import com.example.sync.ui.screens.home.HomeViewModel
import com.example.sync.ui.screens.lostfound.LostFoundScreen
import com.example.sync.ui.screens.lostfound.LostFoundViewModel
import com.example.sync.ui.screens.materials.MaterialsScreen
import com.example.sync.ui.screens.materials.MaterialsViewModel
import com.example.sync.ui.screens.polls.PollsScreen
import com.example.sync.ui.screens.polls.PollsViewModel
import com.example.sync.ui.screens.profile.ProfileScreen
import com.example.sync.ui.screens.profile.ProfileViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * The root Composable for the SynC application.
 * Manages Navigation, Authentication state synchronization, and the global Scaffold (Bottom Bar).
 */
@Composable
fun SynCApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // Manual Dependency Injection: Providing managers and repositories to ViewModels
    val authManager = remember { AuthManager(context) }
    val repository = remember { CampusRepository(RetrofitClient.apiService) }
    val factory = remember { ViewModelFactory(repository, authManager) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    /**
     * Reacts to changes in the Authentication Token.
     * 1. Updates the Retrofit client's header interceptor.
     * 2. Triggers a redirect to Login if the user is logged out (token becomes null).
     */
    LaunchedEffect(authManager.authToken) {
        authManager.authToken.collectLatest { token ->
            RetrofitClient.setAuthToken(token)
            
            if (token == null) {
                val currentDest = navController.currentDestination?.route
                // Prevent navigation loops if already on auth screens
                if (currentDest != null && currentDest != "splash" && currentDest != "login" && currentDest != "register") {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    // Define which screens should display the Bottom Navigation Bar
    val mainScreens = listOf("home", "polls", "materials", "lost-found", "profile")
    val showBottomBar = currentRoute in mainScreens

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                GlassBottomNavigation(
                    selectedIndex = when (currentRoute) {
                        "home" -> 0
                        "polls" -> 1
                        "materials" -> 2
                        "lost-found" -> 3
                        "profile" -> 4
                        else -> 0
                    },
                    onItemSelected = { index ->
                        val targetRoute = when (index) {
                            0 -> "home"
                            1 -> "polls"
                            2 -> "materials"
                            3 -> "lost-found"
                            4 -> "profile"
                            else -> "home"
                        }
                        // Perform standard navigation with state restoration
                        if (currentRoute != targetRoute) {
                            navController.navigate(targetRoute) {
                                navController.graph.startDestinationRoute?.let { startRoute ->
                                    popUpTo(startRoute) {
                                        saveState = true
                                    }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        // Central Navigation Host defining all app destinations
        NavHost(
            navController = navController, 
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- Splash Screen: Handles session verification on startup ---
            composable("splash") {
                val splashViewModel: SplashViewModel = viewModel(factory = factory)
                SplashScreen(
                    viewModel = splashViewModel,
                    onNavigateToHome = {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }
            
            // --- Authentication Screens ---
            composable("login") {
                val loginViewModel: LoginViewModel = viewModel(factory = factory)
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onForgotPasswordClick = { /* Handle forgot password flow */ },
                    onRegisterClick = { navController.navigate("register") }
                )
            }
            composable("register") {
                val registerViewModel: RegisterViewModel = viewModel(factory = factory)
                RegisterScreen(
                    viewModel = registerViewModel,
                    onRegisterSuccess = { navController.navigate("login") },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
            
            // --- Core Feature Screens (Main Dashboard) ---
            composable("home") {
                val homeViewModel: HomeViewModel = viewModel(factory = factory)
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToPolls = { navController.navigate("polls") },
                    onNavigateToLostFound = { navController.navigate("lost-found") }
                )
            }
            composable("polls") {
                val pollsViewModel: PollsViewModel = viewModel(factory = factory)
                PollsScreen(viewModel = pollsViewModel)
            }
            composable("materials") {
                val materialsViewModel: MaterialsViewModel = viewModel(factory = factory)
                MaterialsScreen(viewModel = materialsViewModel)
            }
            composable("lost-found") {
                val lostFoundViewModel: LostFoundViewModel = viewModel(factory = factory)
                LostFoundScreen(viewModel = lostFoundViewModel)
            }
            composable("profile") {
                val profileViewModel: ProfileViewModel = viewModel(factory = factory)
                ProfileScreen(viewModel = profileViewModel)
            }
        }
    }
}

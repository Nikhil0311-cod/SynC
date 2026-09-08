package com.example.sync.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sync.R

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val navState by viewModel.navState.collectAsStateWithLifecycle()

    LaunchedEffect(navState) {
        when (navState) {
            SplashNavigationState.Authenticated -> onNavigateToHome()
            SplashNavigationState.Unauthenticated -> onNavigateToLogin()
            SplashNavigationState.Loading -> { /* Stay on splash */ }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.sync_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(200.dp)
        )
    }
}

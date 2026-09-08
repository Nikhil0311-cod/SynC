package com.example.sync.ui.screens.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sync.R
import com.example.sync.ui.components.GlassButton
import com.example.sync.ui.theme.SynCTheme

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = SynCTheme.glassColors

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(glassColors.backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.sync_logo),
                contentDescription = "SynC Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "Welcome to SynC",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = glassColors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Seamless Collaboration for Modern Teams",
                style = MaterialTheme.typography.bodyLarge,
                color = glassColors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            GlassButton(
                onClick = onGetStarted,
                text = "Join now",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Already have an account? Sign in",
                    color = glassColors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

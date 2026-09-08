package com.example.sync

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * A dedicated Splash Activity to show the brand logo on app startup.
 * It uses a simple Jetpack Compose layout and navigates to [MainActivity] after a short delay.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Splash screen container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White), // White background for the logo
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sync_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(200.dp)
                )
            }

            // Handle navigation logic after the logo display
            LaunchedEffect(key1 = true) {
                // Show logo for 2 seconds
                delay(2000)
                // Transition to the main activity
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                // Remove splash from the back stack
                finish()
            }
        }
    }
}

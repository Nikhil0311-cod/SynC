package com.example.sync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.sync.ui.SynCApp
import com.example.sync.ui.theme.SynCTheme

/**
 * The main entry point of the SynC application after the splash screen.
 * This activity hosts the primary Compose UI and sets up edge-to-edge support.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enables edge-to-edge display, allowing content to flow behind system bars
        enableEdgeToEdge()
        
        setContent {
            // Apply the custom application theme (Glassmorphism inspired)
            SynCTheme {
                // Main app navigation and screen orchestration
                SynCApp()
            }
        }
    }
}

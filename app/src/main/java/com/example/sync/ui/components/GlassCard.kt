package com.example.sync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.sync.ui.theme.SynCTheme
import androidx.compose.material3.Text

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush

/**
 * A custom Compose component that implements a "Glassmorphism" effect.
 * It features a semi-transparent background (frosted glass), a multi-layered border, 
 * and a subtle inner highlight to simulate depth.
 *
 * @param modifier Modifier to be applied to the outer container.
 * @param containerColor The background color of the card, usually semi-transparent.
 * @param content The Composable content to be displayed inside the card.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    containerColor: Color = SynCTheme.glassColors.glassWhite,
    content: @Composable BoxScope.() -> Unit
) {
    val glassColors = SynCTheme.glassColors
    Box(
        modifier = modifier
            // Apply a deep shadow for a floating effect
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = Color.Black.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(containerColor)
            // Multi-color linear gradient border to simulate light reflection
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        glassColors.glassBorder.copy(alpha = 0.8f),
                        glassColors.glassBorder.copy(alpha = 0.1f),
                        glassColors.glassBorder.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        // Subtle inner highlight (top-down gradient) to enhance the glass look
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            glassColors.glassBorder.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Content container with default padding
        Box(modifier = Modifier.padding(24.dp)) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GlassCardPreview() {
    SynCTheme {
        Box(
            modifier = Modifier
                .size(400.dp)
                .background(Brush.linearGradient(listOf(Color(0xFF667eea), Color(0xFF764ba2)))),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(modifier = Modifier.padding(24.dp)) {
                Column {
                    Text(
                        text = "Glassmorphism",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SynCTheme.glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This is a frosted glass card effect.",
                        color = SynCTheme.glassColors.textSecondary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GlassCardDarkPreview() {
    SynCTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(20.dp)) {
            GlassCard {
                Text(text = "Frosted Glass Effect", color = SynCTheme.glassColors.textPrimary)
            }
        }
    }
}

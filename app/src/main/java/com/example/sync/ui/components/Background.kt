package com.example.sync.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.sync.ui.theme.LocalGradientColors

@Composable
fun SynCBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val gradientColors = LocalGradientColors.current
    Surface(
        color = if (gradientColors.container != Color.Unspecified) {
            gradientColors.container
        } else {
            Color.Transparent
        },
        modifier = modifier.fillMaxSize(),
    ) {
        CompositionLocalProvider(LocalAbsoluteTonalElevation provides 0.dp) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (gradientColors.top != Color.Unspecified && gradientColors.bottom != Color.Unspecified) {
                            Modifier.drawWithCache {
                                val brush = Brush.verticalGradient(
                                    colors = listOf(gradientColors.top, gradientColors.bottom),
                                )
                                onDrawBehind {
                                    drawRect(brush)
                                }
                            }
                        } else {
                            Modifier
                        }
                    ),
            ) {
                content()
            }
        }
    }
}

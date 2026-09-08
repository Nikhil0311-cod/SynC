package com.example.sync.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class GlassColors(
    val glassWhite: Color,
    val glassBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val backgroundGradient: Brush,
    val borderHighlight: Color,
    val borderNormal: Color
)

val LocalGlassColors = staticCompositionLocalOf<GlassColors> {
    error("No GlassColors provided")
}

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    secondary = PurpleGrey80,
    tertiary = AccentCyan,
    background = BgGradientStartDark,
    surface = Color(0xFF0D1117),
    onPrimary = Color.White,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = BgGradientStartLight,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight
)

@Composable
fun SynCTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    val glassColors = if (darkTheme) {
        GlassColors(
            glassWhite = GlassWhiteDark,
            glassBorder = GlassBorderDark,
            textPrimary = TextPrimaryDark,
            textSecondary = TextSecondaryDark,
            backgroundGradient = Brush.verticalGradient(
                colors = listOf(BgGradientStartDark, BgGradientMidDark, BgGradientEndDark)
            ),
            borderHighlight = AccentBlue.copy(alpha = 0.7f),
            borderNormal = GlassBorderDark
        )
    } else {
        GlassColors(
            glassWhite = GlassWhiteLight,
            glassBorder = GlassBorderLight,
            textPrimary = TextPrimaryLight,
            textSecondary = TextSecondaryLight,
            backgroundGradient = Brush.verticalGradient(
                colors = listOf(BgGradientStartLight, BgGradientEndLight)
            ),
            borderHighlight = AccentBlue.copy(alpha = 0.8f),
            borderNormal = GlassBorderLight
        )
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalGlassColors provides glassColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object SynCTheme {
    val glassColors: GlassColors
        @Composable
        get() = LocalGlassColors.current
}

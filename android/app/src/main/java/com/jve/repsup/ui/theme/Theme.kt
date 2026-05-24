package com.jve.repsup.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Reps Up - Theme

private val DarkColourScheme = darkColorScheme(
    // Main accent colour
    primary = LightBlue,
    onPrimary = DarkGrey,  // text/icons on primary colour

    // Rare accents
    secondary = Red,
    tertiary = Yellow,

    // Background
    background = DarkGrey,
    onBackground = White,

    // Surface (cards on top of background)
    surface = DarkGreyVariant,
    onSurface = White,
    surfaceVariant = DarkGreySurface,
    onSurfaceVariant = Muted,

    // Error
    error = Red,
)

@Composable
fun RepsUpTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColourScheme

    // Force system status bar to match our dark theme
    val view = LocalView.current
    if (!view.isInEditMode){
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
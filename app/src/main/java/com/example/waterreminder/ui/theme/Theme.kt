package com.example.waterreminder.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF005FB8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9ECAFF),
    onPrimaryContainer = Color(0xFF001B3E),
    secondaryContainer = Color(0xFFC6E7FF),
    onSecondaryContainer = Color(0xFF001E2D),
    background = Color(0xFFFDF8FF),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFDF8FF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF2E83EB),
    onPrimary = Color(0xFF00214F),
    primaryContainer = Color(0xFF7BB2E3),
    onPrimaryContainer = Color(0xFF00214F),
    secondaryContainer = Color(0xFF00566F),
    onSecondaryContainer = Color(0xFFC6E7FF),
    background = Color(0xFF131218),
    onBackground = Color(0xFFE4E1E9),
    surface = Color(0xFF131218),
    onSurface = Color(0xFFE4E1E9),
    surfaceVariant = Color(0xFF45464F),
    onSurfaceVariant = Color(0xFFC6C6D0),
    outline = Color(0xFF90909A),
    outlineVariant = Color(0xFF45464F)
)

@Composable
fun WaterTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}

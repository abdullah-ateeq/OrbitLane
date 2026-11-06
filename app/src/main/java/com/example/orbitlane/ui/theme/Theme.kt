package com.example.orbitlane.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = SpaceBlack,
    primaryContainer = GraphiteCard,
    onPrimaryContainer = CyanAccent,
    secondary = ElectricBlue,
    onSecondary = SpaceBlack,
    background = SpaceBlack,
    onBackground = TextPrimary,
    surface = GraphiteSurface,
    onSurface = TextPrimary,
    surfaceVariant = GraphiteCard,
    onSurfaceVariant = TextSecondary,
    outline = GraphiteBorder,
    error = NeonCoral,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = LightCard,
    onPrimaryContainer = ElectricBlue,
    secondary = CyanAccent,
    onSecondary = SpaceBlack,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightCard,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = NeonCoral,
    onError = Color.White
)

@Composable
fun OrbitLaneTheme(
    themePreference: String = "DARK", // "SYSTEM", "DARK", "LIGHT"
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themePreference) {
        "LIGHT" -> false
        "DARK" -> true
        else -> systemInDark
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

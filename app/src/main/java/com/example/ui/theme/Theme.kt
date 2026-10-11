package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.Black,
    primaryContainer = BrandPrimaryContainer,
    onPrimaryContainer = Color.White,
    secondary = BrandSecondary,
    onSecondary = Color.Black,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

private val AmoledColorScheme = darkColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.Black,
    primaryContainer = BrandPrimaryContainer,
    onPrimaryContainer = Color.White,
    secondary = BrandSecondary,
    onSecondary = Color.Black,
    background = AmoledBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFECE7FF),
    onPrimaryContainer = Color(0xFF271C50),
    secondary = BrandSecondary,
    onSecondary = Color.White,
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF191C20),
    surface = Color.White,
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFF1F3F5),
    onSurfaceVariant = Color(0xFF495057),
    outline = Color(0xFFE9ECEF)
)

enum class ThemeMode {
    SYSTEM,
    DARK,
    AMOLED,
    LIGHT
}

@Composable
fun HabitTrackerTheme(
    themeMode: ThemeMode = ThemeMode.AMOLED,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        ThemeMode.AMOLED -> AmoledColorScheme
        ThemeMode.DARK -> DarkColorScheme
        ThemeMode.LIGHT -> LightColorScheme
        ThemeMode.SYSTEM -> if (systemDark) DarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

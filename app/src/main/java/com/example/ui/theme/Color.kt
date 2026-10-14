package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Habit Accent Colors (as seen in HabitKit screenshots)
val HabitLime = Color(0xFF8AE034)
val HabitCyan = Color(0xFF26D3EB)
val HabitAmber = Color(0xFFF7BA32)
val HabitPurple = Color(0xFFA28BF7)
val HabitMint = Color(0xFF3BD9A5)
val HabitPink = Color(0xFFF06595)
val HabitCoral = Color(0xFFFA5252)
val HabitSky = Color(0xFF339AF0)
val HabitOrange = Color(0xFFFD7E14)
val HabitPeriwinkle = Color(0xFF748FFC)
val HabitYellow = Color(0xFFFCC419)
val HabitLavender = Color(0xFFD0BFFF)
val HabitDeepTeal = Color(0xFF20C997)
val HabitRose = Color(0xFFFF6B81)
val HabitSlate = Color(0xFF9099A2)
val HabitSand = Color(0xFFD4A373)

// Dark Palette
val DarkBackground = Color(0xFF0E1015)
val DarkSurface = Color(0xFF16181F)
val DarkSurfaceElevated = Color(0xFF1E212A)
val DarkSurfaceVariant = Color(0xFF272A35)
val DarkBorder = Color(0xFF272A35)
val DarkTextPrimary = Color(0xFFF8F9FA)
val DarkTextSecondary = Color(0xFF9AA0A6)
val DarkTextMuted = Color(0xFF5F6368)

// Accent / Action
val BrandPrimary = Color(0xFFA28BF7)
val BrandPrimaryContainer = Color(0xFF362C5C)
val BrandSecondary = Color(0xFF26D3EB)

/**
 * Theme-aware UI chrome palette. All values that were previously hardcoded
 * dark hexes across screens/components resolve through here so light mode
 * renders correctly. Dark values preserve the original look exactly.
 */
@Immutable
data class AppChromeColors(
    val container: Color,
    val containerAlt: Color,
    val chip: Color,
    val border: Color,
    val muted: Color,
    val selectedNeutral: Color,
    val tileInactive: Color,
    val tileDim: Color,
    val glassContainer: Color,
    val glassLiveContainer: Color,
    val dangerContainer: Color,
    val dangerBorder: Color,
    val dangerText: Color,
    val dangerSubText: Color,
    val infoContainer: Color,
    val infoBorder: Color,
    val infoText: Color,
    val infoSubText: Color
)

val DarkChrome = AppChromeColors(
    container = Color(0xFF1E212A),
    containerAlt = Color(0xFF1B1E26),
    chip = Color(0xFF161820),
    border = Color(0xFF282C38),
    muted = Color(0xFF8E93A3),
    selectedNeutral = Color(0xFF3A3F4E),
    tileInactive = Color(0xFF1A1C24),
    tileDim = Color(0xFF13151A),
    glassContainer = Color(0xFF16181F).copy(alpha = 0.72f),
    glassLiveContainer = Color(0xFF16181F).copy(alpha = 0.60f),
    dangerContainer = Color(0xFF231719),
    dangerBorder = Color(0xFF4A2026),
    dangerText = Color(0xFFFF6B6B),
    dangerSubText = Color(0xFFC78282),
    infoContainer = Color(0xFF161E28),
    infoBorder = Color(0xFF233B54),
    infoText = Color(0xFF4DABF7),
    infoSubText = Color(0xFF7FA7C7)
)

val LightChrome = AppChromeColors(
    container = Color(0xFFE9ECEF),
    containerAlt = Color(0xFFF1F3F5),
    chip = Color(0xFFF1F3F5),
    border = Color(0xFFDDE2E6),
    muted = Color(0xFF6C757D),
    selectedNeutral = Color(0xFFD8DDE2),
    tileInactive = Color(0xFFE4E7EB),
    tileDim = Color(0xFFF1F3F5),
    glassContainer = Color(0xFFFFFFFF).copy(alpha = 0.75f),
    glassLiveContainer = Color(0xFFFFFFFF).copy(alpha = 0.65f),
    dangerContainer = Color(0xFFFDECEC),
    dangerBorder = Color(0xFFF5C2C7),
    dangerText = Color(0xFFC92A2A),
    dangerSubText = Color(0xFFA15C5C),
    infoContainer = Color(0xFFE7F1FA),
    infoBorder = Color(0xFFB6D4E8),
    infoText = Color(0xFF1971C2),
    infoSubText = Color(0xFF4D7FA3)
)

val LocalAppChrome = compositionLocalOf { DarkChrome }

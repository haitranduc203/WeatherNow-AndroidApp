package com.example.weathernow.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// Stitch Design System — WeatherNow Colors
// ==========================================

// Surface & Background (Deep Atmospheric Navy)
val WeatherBackgroundDark = Color(0xFF0F131A)
val WeatherSurfaceDark = Color(0xFF151A23)
val WeatherSurfaceDim = Color(0xFF0F131A)
val WeatherSurfaceBrightDark = Color(0xFF2E3440)
val WeatherSurfaceContainerLowest = Color(0xFF0A0D12)
val WeatherSurfaceContainerLow = Color(0xFF131821)
val WeatherSurfaceContainer = Color(0xFF181E27)
val WeatherSurfaceContainerHigh = Color(0xFF202732)
val WeatherSurfaceContainerHighest = Color(0xFF28303E)
val WeatherSurfaceVariant = Color(0xFF28303E)

// Typography & Content (Dark Mode)
val WeatherOnSurfaceDark = Color(0xFFF1F5F9)
val WeatherOnSurfaceVariantDark = Color(0xFF94A3B8)
val WeatherOnBackgroundDark = Color(0xFFF1F5F9)
val WeatherOutlineDark = Color(0xFF64748B)
val WeatherOutlineVariantDark = Color(0xFF334155)

// Primary (Indigo / Aurora Blue)
val WeatherPrimary = Color(0xFFBDC2FF)
val WeatherOnPrimary = Color(0xFF1B247F)
val WeatherPrimaryContainer = Color(0xFF2D358E)
val WeatherOnPrimaryContainer = Color(0xFFDFE1FF)
val WeatherPrimaryFixed = Color(0xFFE0E0FF)

// Secondary (Sky / Cyan Blue)
val WeatherSecondary = Color(0xFF96CCFF)
val WeatherOnSecondary = Color(0xFF003353)
val WeatherSecondaryContainer = Color(0xFF004B75)
val WeatherOnSecondaryContainer = Color(0xFFCEE5FF)
val WeatherSecondaryFixed = Color(0xFFCEE5FF)

// Tertiary / Accent (Solar Amber & Gold)
val WeatherTertiary = Color(0xFFFABD00)
val WeatherOnTertiary = Color(0xFF3F2E00)
val WeatherTertiaryContainer = Color(0xFF5B4300)
val WeatherOnTertiaryContainer = Color(0xFFFFDF94)

// Status & Indicators
val WeatherError = Color(0xFFFFB4AB)
val WeatherOnError = Color(0xFF690005)
val WeatherErrorContainer = Color(0xFF93000A)
val WeatherSuccess = Color(0xFF81C784)
val WeatherWarning = Color(0xFFFFB74D)

// ==========================================
// Light Mode Palette
// ==========================================
val WeatherBackgroundLight = Color(0xFFF1F5FB)
val WeatherSurfaceLight = Color(0xFFFFFFFF)
val WeatherSurfaceContainerLight = Color(0xFFE8EEF8)
val WeatherSurfaceContainerHighLight = Color(0xFFDDE5F2)
val WeatherOnSurfaceLight = Color(0xFF0F172A)
val WeatherOnSurfaceVariantLight = Color(0xFF475569)
val WeatherPrimaryLight = Color(0xFF3749B8)
val WeatherOnPrimaryLight = Color(0xFFFFFFFF)
val WeatherPrimaryContainerLight = Color(0xFFDFE2FF)
val WeatherOnPrimaryContainerLight = Color(0xFF00115B)
val WeatherSecondaryLight = Color(0xFF0284C7)
val WeatherOnSecondaryLight = Color(0xFFFFFFFF)
val WeatherSecondaryContainerLight = Color(0xFFD0E8FF)
val WeatherOnSecondaryContainerLight = Color(0xFF001E31)
val WeatherTertiaryLight = Color(0xFFB45309)
val WeatherOnTertiaryLight = Color(0xFFFFFFFF)

// ==========================================
// Glassmorphism & Atmospheric Gradient Helpers
// ==========================================
val GlassCardBackgroundDark = Color(0xFF181E28).copy(alpha = 0.85f)
val GlassCardBackgroundLight = Color(0xFFFFFFFF).copy(alpha = 0.90f)
val GlassCardBorderDark = Color(0xFFFFFFFF).copy(alpha = 0.12f)
val GlassCardBorderLight = Color(0xFFE2E8F0)
val GlassCardSubtleDark = Color(0xFFFFFFFF).copy(alpha = 0.05f)

val AtmosphericGradientDark = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF161C28),
        Color(0xFF0F131A),
        Color(0xFF0A0D12)
    )
)

val AtmosphericGradientLight = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFE5EDF9),
        Color(0xFFEEF3FA),
        Color(0xFFF8FAFC)
    )
)

@Composable
fun atmosphericGradient(): Brush {
    val isDark = androidx.compose.material3.MaterialTheme.colorScheme.background == WeatherBackgroundDark
    return if (isDark) AtmosphericGradientDark else AtmosphericGradientLight
}

val HeroWeatherGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF283593).copy(alpha = 0.5f),
        Color(0xFF0288D1).copy(alpha = 0.25f),
        Color(0xFF10141A).copy(alpha = 0.7f)
    )
)

val TemperatureRangeGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF29B6F6), // Cool Low
        Color(0xFF81C784), // Mild
        Color(0xFFFFA726), // Warm High
        Color(0xFFFF7043)  // Hot High
    )
)

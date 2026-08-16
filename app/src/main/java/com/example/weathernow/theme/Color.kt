package com.example.weathernow.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// Stitch Design System — WeatherNow Colors
// ==========================================

// Surface & Background (Deep Atmospheric Navy)
val WeatherBackgroundDark = Color(0xFF10141A)
val WeatherSurfaceDark = Color(0xFF10141A)
val WeatherSurfaceDim = Color(0xFF10141A)
val WeatherSurfaceBrightDark = Color(0xFF353940)
val WeatherSurfaceContainerLowest = Color(0xFF0A0E14)
val WeatherSurfaceContainerLow = Color(0xFF181C22)
val WeatherSurfaceContainer = Color(0xFF1C2026)
val WeatherSurfaceContainerHigh = Color(0xFF262A31)
val WeatherSurfaceContainerHighest = Color(0xFF31353C)
val WeatherSurfaceVariant = Color(0xFF31353C)

// Typography & Content
val WeatherOnSurface = Color(0xFFDFE2EB)
val WeatherOnSurfaceVariant = Color(0xFFC6C5D4)
val WeatherOnBackground = Color(0xFFDFE2EB)
val WeatherOutline = Color(0xFF908F9D)
val WeatherOutlineVariant = Color(0xFF454652)

// Primary (Indigo / Aurora Blue)
val WeatherPrimary = Color(0xFFBDC2FF)
val WeatherOnPrimary = Color(0xFF1B247F)
val WeatherPrimaryContainer = Color(0xFF1A237E)
val WeatherOnPrimaryContainer = Color(0xFF8690EE)
val WeatherPrimaryFixed = Color(0xFFE0E0FF)

// Secondary (Sky / Cyan Blue)
val WeatherSecondary = Color(0xFF96CCFF)
val WeatherOnSecondary = Color(0xFF003353)
val WeatherSecondaryContainer = Color(0xFF2B97E1)
val WeatherOnSecondaryContainer = Color(0xFF002C48)
val WeatherSecondaryFixed = Color(0xFFCEE5FF)

// Tertiary / Accent (Solar Amber & Gold)
val WeatherTertiary = Color(0xFFFABD00)
val WeatherOnTertiary = Color(0xFF3F2E00)
val WeatherTertiaryContainer = Color(0xFF3F2D00)
val WeatherOnTertiaryContainer = Color(0xFFC09000)

// Status & Indicators
val WeatherError = Color(0xFFFFB4AB)
val WeatherOnError = Color(0xFF690005)
val WeatherErrorContainer = Color(0xFF93000A)
val WeatherSuccess = Color(0xFF81C784)
val WeatherWarning = Color(0xFFFFB74D)

// ==========================================
// Light Mode Palette
// ==========================================
val WeatherBackgroundLight = Color(0xFFF6F8FC)
val WeatherSurfaceLight = Color(0xFFFFFFFF)
val WeatherSurfaceContainerLight = Color(0xFFEDF2F7)
val WeatherSurfaceContainerHighLight = Color(0xFFE2E8F0)
val WeatherOnSurfaceLight = Color(0xFF1E293B)
val WeatherOnSurfaceVariantLight = Color(0xFF64748B)
val WeatherPrimaryLight = Color(0xFF3F51B5)
val WeatherPrimaryContainerLight = Color(0xFFE8EAF6)
val WeatherSecondaryLight = Color(0xFF0288D1)
val WeatherSecondaryContainerLight = Color(0xFFE1F5FE)

// ==========================================
// Glassmorphism & Atmospheric Gradient Helpers
// ==========================================
val GlassCardBackgroundDark = Color(0xFFFFFFFF).copy(alpha = 0.08f)
val GlassCardBackgroundLight = Color(0xFFFFFFFF).copy(alpha = 0.85f)
val GlassCardBorderDark = Color(0xFFFFFFFF).copy(alpha = 0.12f)
val GlassCardBorderLight = Color(0xFF000000).copy(alpha = 0.06f)
val GlassCardSubtleDark = Color(0xFFFFFFFF).copy(alpha = 0.04f)

val AtmosphericGradientDark = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF1A237E).copy(alpha = 0.35f),
        Color(0xFF10141A),
        Color(0xFF0A0E14)
    )
)

val AtmosphericGradientLight = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFE3F2FD),
        Color(0xFFF6F8FC),
        Color(0xFFFFFFFF)
    )
)

val HeroWeatherGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF283593).copy(alpha = 0.6f),
        Color(0xFF0288D1).copy(alpha = 0.3f),
        Color(0xFF10141A).copy(alpha = 0.8f)
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

package com.example.weathernow.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = WeatherPrimary,
    onPrimary = WeatherOnPrimary,
    primaryContainer = WeatherPrimaryContainer,
    onPrimaryContainer = WeatherOnPrimaryContainer,
    secondary = WeatherSecondary,
    onSecondary = WeatherOnSecondary,
    secondaryContainer = WeatherSecondaryContainer,
    onSecondaryContainer = WeatherOnSecondaryContainer,
    tertiary = WeatherTertiary,
    onTertiary = WeatherOnTertiary,
    tertiaryContainer = WeatherTertiaryContainer,
    onTertiaryContainer = WeatherOnTertiaryContainer,
    background = WeatherBackgroundDark,
    onBackground = WeatherOnBackgroundDark,
    surface = WeatherSurfaceDark,
    onSurface = WeatherOnSurfaceDark,
    surfaceVariant = WeatherSurfaceVariant,
    onSurfaceVariant = WeatherOnSurfaceVariantDark,
    outline = WeatherOutlineDark,
    outlineVariant = WeatherOutlineVariantDark,
    error = WeatherError,
    onError = WeatherOnError,
    errorContainer = WeatherErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = WeatherPrimaryLight,
    onPrimary = WeatherOnPrimaryLight,
    primaryContainer = WeatherPrimaryContainerLight,
    onPrimaryContainer = WeatherOnPrimaryContainerLight,
    secondary = WeatherSecondaryLight,
    onSecondary = WeatherOnSecondaryLight,
    secondaryContainer = WeatherSecondaryContainerLight,
    onSecondaryContainer = WeatherOnSecondaryContainerLight,
    tertiary = WeatherTertiaryLight,
    onTertiary = WeatherOnTertiaryLight,
    background = WeatherBackgroundLight,
    onBackground = WeatherOnSurfaceLight,
    surface = WeatherSurfaceLight,
    onSurface = WeatherOnSurfaceLight,
    surfaceVariant = WeatherSurfaceContainerLight,
    onSurfaceVariant = WeatherOnSurfaceVariantLight,
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFCBD5E1),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6)
)

@Composable
fun WeatherNowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

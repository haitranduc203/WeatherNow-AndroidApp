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
    onBackground = WeatherOnBackground,
    surface = WeatherSurfaceDark,
    onSurface = WeatherOnSurface,
    surfaceVariant = WeatherSurfaceVariant,
    onSurfaceVariant = WeatherOnSurfaceVariant,
    outline = WeatherOutline,
    outlineVariant = WeatherOutlineVariant,
    error = WeatherError,
    onError = WeatherOnError,
    errorContainer = WeatherErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = WeatherPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = WeatherPrimaryContainerLight,
    onPrimaryContainer = WeatherPrimaryLight,
    secondary = WeatherSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = WeatherSecondaryContainerLight,
    onSecondaryContainer = WeatherSecondaryLight,
    tertiary = WeatherTertiary,
    onTertiary = Color.White,
    background = WeatherBackgroundLight,
    onBackground = WeatherOnSurfaceLight,
    surface = WeatherSurfaceLight,
    onSurface = WeatherOnSurfaceLight,
    surfaceVariant = WeatherSurfaceContainerLight,
    onSurfaceVariant = WeatherOnSurfaceVariantLight,
    outline = WeatherOutline,
    outlineVariant = WeatherOutlineVariant
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

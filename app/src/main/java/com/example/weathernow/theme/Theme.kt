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
    primary = WeatherBlueNeon,
    secondary = WeatherSunGoldDark,
    tertiary = WeatherSkyBlueLight,
    background = WeatherDeepMidnight,
    surface = WeatherSurfaceDark,
    surfaceVariant = WeatherCardDark,
    onPrimary = WeatherDeepMidnight,
    onSecondary = WeatherDeepMidnight,
    onBackground = WeatherTextPrimaryDark,
    onSurface = WeatherTextPrimaryDark,
    onSurfaceVariant = WeatherTextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = WeatherSkyBlue,
    secondary = WeatherSunGold,
    tertiary = WeatherSkyBlueLight,
    background = WeatherBackgroundLight,
    surface = WeatherCardLight,
    surfaceVariant = WeatherCardLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = WeatherTextPrimaryLight,
    onSurface = WeatherTextPrimaryLight,
    onSurfaceVariant = WeatherTextSecondaryLight
)

@Composable
fun WeatherNowTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

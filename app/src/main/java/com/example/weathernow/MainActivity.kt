package com.example.weathernow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.weathernow.domain.model.AppTheme
import com.example.weathernow.presentation.navigation.WeatherNavHost
import com.example.weathernow.presentation.settings.UserPreferencesRepository
import com.example.weathernow.presentation.util.ProvideWeatherLanguage
import com.example.weathernow.theme.WeatherNowTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      val preferences by UserPreferencesRepository.preferencesFlow.collectAsState()
      val isDark = when (preferences.theme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
      }

      WeatherNowTheme(darkTheme = isDark) {
        ProvideWeatherLanguage(language = preferences.language) {
          Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
          ) {
            WeatherNavHost()
          }
        }
      }
    }
  }
}

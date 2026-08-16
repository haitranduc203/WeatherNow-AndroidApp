package com.example.weathernow.domain.model

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT
}

enum class WindSpeedUnit {
    KMH,
    MPH,
    MS
}

data class UserPreferences(
    val theme: AppTheme = AppTheme.SYSTEM,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KMH,
    val notificationsEnabled: Boolean = false,
    val backgroundRefreshEnabled: Boolean = false,
    val isOnboardingCompleted: Boolean = false
)

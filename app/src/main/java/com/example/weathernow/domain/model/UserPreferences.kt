package com.example.weathernow.domain.model

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    VIETNAMESE("vi", "Tiếng Việt")
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
    val language: AppLanguage = AppLanguage.VIETNAMESE,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KMH,
    val notificationsEnabled: Boolean = false,
    val dailyNotificationEnabled: Boolean = false,
    val severeWeatherAlertsEnabled: Boolean = false,
    val backgroundRefreshEnabled: Boolean = false,
    val backgroundRefreshIntervalHours: Int = 3,
    val isOnboardingCompleted: Boolean = false
)

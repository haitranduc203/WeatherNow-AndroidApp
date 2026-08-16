package com.example.weathernow.domain.repository

import com.example.weathernow.domain.model.AppLanguage
import com.example.weathernow.domain.model.AppTheme
import com.example.weathernow.domain.model.TemperatureUnit
import com.example.weathernow.domain.model.UserPreferences
import com.example.weathernow.domain.model.WindSpeedUnit
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferences>

    suspend fun setTheme(theme: AppTheme)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setTemperatureUnit(unit: TemperatureUnit)
    suspend fun setWindSpeedUnit(unit: WindSpeedUnit)
    suspend fun setDailyNotificationEnabled(enabled: Boolean)
    suspend fun setSevereAlertsEnabled(enabled: Boolean)
    suspend fun setBackgroundRefreshEnabled(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
}

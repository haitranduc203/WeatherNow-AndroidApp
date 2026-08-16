package com.example.weathernow.domain.repository

import com.example.weathernow.domain.model.AppTheme
import com.example.weathernow.domain.model.TemperatureUnit
import com.example.weathernow.domain.model.UserPreferences
import com.example.weathernow.domain.model.WindSpeedUnit
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val userPreferences: Flow<UserPreferences>
    suspend fun setTheme(theme: AppTheme)
    suspend fun setTemperatureUnit(unit: TemperatureUnit)
    suspend fun setWindSpeedUnit(unit: WindSpeedUnit)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setBackgroundRefreshEnabled(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
}

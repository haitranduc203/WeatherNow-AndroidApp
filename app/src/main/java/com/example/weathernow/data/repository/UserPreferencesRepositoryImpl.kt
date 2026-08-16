package com.example.weathernow.data.repository

import com.example.weathernow.data.local.datastore.UserPreferencesDataStore
import com.example.weathernow.domain.model.AppLanguage
import com.example.weathernow.domain.model.AppTheme
import com.example.weathernow.domain.model.TemperatureUnit
import com.example.weathernow.domain.model.UserPreferences
import com.example.weathernow.domain.model.WindSpeedUnit
import com.example.weathernow.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class UserPreferencesRepositoryImpl(
    private val dataStore: UserPreferencesDataStore
) : UserPreferencesRepository {

    override val userPreferences: Flow<UserPreferences> = dataStore.userPreferencesFlow

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.updateTheme(theme)
    }

    override suspend fun setLanguage(language: AppLanguage) {
        dataStore.updateLanguage(language)
    }

    override suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        dataStore.updateTemperatureUnit(unit)
    }

    override suspend fun setWindSpeedUnit(unit: WindSpeedUnit) {
        dataStore.updateWindSpeedUnit(unit)
    }

    override suspend fun setDailyNotificationEnabled(enabled: Boolean) {
        dataStore.updateDailyNotification(enabled)
    }

    override suspend fun setSevereAlertsEnabled(enabled: Boolean) {
        dataStore.updateSevereAlerts(enabled)
    }

    override suspend fun setBackgroundRefreshEnabled(enabled: Boolean) {
        dataStore.updateBackgroundRefresh(enabled)
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.updateOnboardingCompleted(completed)
    }
}

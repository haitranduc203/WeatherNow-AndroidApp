package com.example.weathernow.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.weathernow.core.common.Constants
import com.example.weathernow.domain.model.AppLanguage
import com.example.weathernow.domain.model.AppTheme
import com.example.weathernow.domain.model.TemperatureUnit
import com.example.weathernow.domain.model.UserPreferences
import com.example.weathernow.domain.model.WindSpeedUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.PREFERENCES_NAME)

class UserPreferencesDataStore(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_THEME = stringPreferencesKey("app_theme")
        val KEY_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_TEMP_UNIT = stringPreferencesKey("temperature_unit")
        val KEY_WIND_UNIT = stringPreferencesKey("wind_speed_unit")
        val KEY_DAILY_NOTIFICATION = booleanPreferencesKey("daily_notification_enabled")
        val KEY_SEVERE_ALERTS = booleanPreferencesKey("severe_alerts_enabled")
        val KEY_BACKGROUND_REFRESH = booleanPreferencesKey("background_refresh_enabled")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeName = preferences[KEY_THEME] ?: AppTheme.SYSTEM.name
            val theme = try { AppTheme.valueOf(themeName) } catch (_: Exception) { AppTheme.SYSTEM }

            val langCode = preferences[KEY_LANGUAGE] ?: AppLanguage.VIETNAMESE.name
            val language = try { AppLanguage.valueOf(langCode) } catch (_: Exception) { AppLanguage.VIETNAMESE }

            val tempUnitName = preferences[KEY_TEMP_UNIT] ?: TemperatureUnit.CELSIUS.name
            val tempUnit = try { TemperatureUnit.valueOf(tempUnitName) } catch (_: Exception) { TemperatureUnit.CELSIUS }

            val windUnitName = preferences[KEY_WIND_UNIT] ?: WindSpeedUnit.KMH.name
            val windUnit = try { WindSpeedUnit.valueOf(windUnitName) } catch (_: Exception) { WindSpeedUnit.KMH }

            val dailyNotif = preferences[KEY_DAILY_NOTIFICATION] ?: false
            val severeAlerts = preferences[KEY_SEVERE_ALERTS] ?: false
            val bgRefresh = preferences[KEY_BACKGROUND_REFRESH] ?: false
            val onboardingCompleted = preferences[KEY_ONBOARDING_COMPLETED] ?: false

            UserPreferences(
                theme = theme,
                language = language,
                temperatureUnit = tempUnit,
                windSpeedUnit = windUnit,
                dailyNotificationEnabled = dailyNotif,
                severeWeatherAlertsEnabled = severeAlerts,
                backgroundRefreshEnabled = bgRefresh,
                isOnboardingCompleted = onboardingCompleted
            )
        }

    suspend fun updateTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme.name
        }
    }

    suspend fun updateLanguage(language: AppLanguage) {
        dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = language.name
        }
    }

    suspend fun updateTemperatureUnit(unit: TemperatureUnit) {
        dataStore.edit { preferences ->
            preferences[KEY_TEMP_UNIT] = unit.name
        }
    }

    suspend fun updateWindSpeedUnit(unit: WindSpeedUnit) {
        dataStore.edit { preferences ->
            preferences[KEY_WIND_UNIT] = unit.name
        }
    }

    suspend fun updateDailyNotification(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DAILY_NOTIFICATION] = enabled
        }
    }

    suspend fun updateSevereAlerts(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SEVERE_ALERTS] = enabled
        }
    }

    suspend fun updateBackgroundRefresh(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_BACKGROUND_REFRESH] = enabled
        }
    }

    suspend fun updateOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }
}

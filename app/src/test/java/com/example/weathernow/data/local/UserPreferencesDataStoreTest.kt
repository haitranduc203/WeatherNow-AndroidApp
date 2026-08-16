package com.example.weathernow.data.local

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import com.example.weathernow.data.local.datastore.UserPreferencesDataStore
import com.example.weathernow.data.repository.UserPreferencesRepositoryImpl
import com.example.weathernow.domain.model.AppLanguage
import com.example.weathernow.domain.model.AppTheme
import com.example.weathernow.domain.model.TemperatureUnit
import com.example.weathernow.domain.model.WindSpeedUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class UserPreferencesDataStoreTest {

    private val testContext: Context = ApplicationProvider.getApplicationContext()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var dataStore: UserPreferencesDataStore
    private lateinit var repository: UserPreferencesRepositoryImpl
    private lateinit var testFile: File

    @Before
    fun setup() {
        testFile = testContext.preferencesDataStoreFile("test_weather_prefs_${System.currentTimeMillis()}")
        val preferencesDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        dataStore = UserPreferencesDataStore(preferencesDataStore)
        repository = UserPreferencesRepositoryImpl(dataStore)
    }

    @Test
    fun `default preferences values are correctly loaded`() = runTest(testDispatcher) {
        val initial = repository.userPreferences.first()
        assertEquals(AppTheme.SYSTEM, initial.theme)
        assertEquals(AppLanguage.VIETNAMESE, initial.language)
        assertEquals(TemperatureUnit.CELSIUS, initial.temperatureUnit)
        assertEquals(WindSpeedUnit.KMH, initial.windSpeedUnit)
        assertFalse(initial.dailyNotificationEnabled)
        assertFalse(initial.severeWeatherAlertsEnabled)
        assertFalse(initial.backgroundRefreshEnabled)
        assertFalse(initial.isOnboardingCompleted)
    }

    @Test
    fun `updating theme persists and emits new value`() = runTest(testDispatcher) {
        repository.setTheme(AppTheme.DARK)
        val updated = repository.userPreferences.first()
        assertEquals(AppTheme.DARK, updated.theme)

        repository.setTheme(AppTheme.LIGHT)
        val updatedLight = repository.userPreferences.first()
        assertEquals(AppTheme.LIGHT, updatedLight.theme)
    }

    @Test
    fun `updating temperature unit persists and emits new value`() = runTest(testDispatcher) {
        repository.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
        val updated = repository.userPreferences.first()
        assertEquals(TemperatureUnit.FAHRENHEIT, updated.temperatureUnit)
    }

    @Test
    fun `updating wind speed unit persists and emits new value`() = runTest(testDispatcher) {
        repository.setWindSpeedUnit(WindSpeedUnit.MPH)
        val updatedMph = repository.userPreferences.first()
        assertEquals(WindSpeedUnit.MPH, updatedMph.windSpeedUnit)

        repository.setWindSpeedUnit(WindSpeedUnit.MS)
        val updatedMs = repository.userPreferences.first()
        assertEquals(WindSpeedUnit.MS, updatedMs.windSpeedUnit)
    }

    @Test
    fun `updating notifications and background refresh toggles persist`() = runTest(testDispatcher) {
        repository.setDailyNotificationEnabled(true)
        repository.setSevereAlertsEnabled(true)
        repository.setBackgroundRefreshEnabled(true)

        val updated = repository.userPreferences.first()
        assertTrue(updated.dailyNotificationEnabled)
        assertTrue(updated.severeWeatherAlertsEnabled)
        assertTrue(updated.backgroundRefreshEnabled)
    }

    @Test
    fun `updating onboarding completed flag persists`() = runTest(testDispatcher) {
        repository.setOnboardingCompleted(true)
        val updated = repository.userPreferences.first()
        assertTrue(updated.isOnboardingCompleted)
    }
}

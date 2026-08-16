package com.example.weathernow.presentation

import com.example.weathernow.domain.model.AppLanguage
import com.example.weathernow.domain.model.AppTheme
import com.example.weathernow.domain.model.TemperatureUnit
import com.example.weathernow.domain.model.WindSpeedUnit
import com.example.weathernow.presentation.settings.SettingsViewModel
import com.example.weathernow.presentation.settings.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testUpdatePreferences_ViaViewModel() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel()
        advanceUntilIdle()

        viewModel.setTheme(AppTheme.DARK)
        viewModel.setLanguage(AppLanguage.VIETNAMESE)
        viewModel.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
        viewModel.setWindSpeedUnit(WindSpeedUnit.MS)
        viewModel.toggleDailySummary(false)
        viewModel.toggleSevereAlerts(true)
        viewModel.toggleBackgroundSync(true)
        advanceUntilIdle()

        val prefs = UserPreferencesRepository.preferencesFlow.value
        assertEquals(AppTheme.DARK, prefs.theme)
        assertEquals(AppLanguage.VIETNAMESE, prefs.language)
        assertEquals(TemperatureUnit.FAHRENHEIT, prefs.temperatureUnit)
        assertEquals(WindSpeedUnit.MS, prefs.windSpeedUnit)
        assertFalse(prefs.dailyNotificationEnabled)
        assertTrue(prefs.severeWeatherAlertsEnabled)
        assertTrue(prefs.backgroundRefreshEnabled)
    }

    @Test
    fun testClearOfflineCache() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel()
        advanceUntilIdle()

        viewModel.clearOfflineCache()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("0 KB", state.cachedDataSize)
        assertEquals("Đã xóa sạch", state.cacheLastCleaned)
    }
}

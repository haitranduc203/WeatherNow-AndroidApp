package com.example.weathernow.presentation

import com.example.weathernow.domain.model.AppLanguage
import com.example.weathernow.domain.model.AppTheme
import com.example.weathernow.domain.model.TemperatureUnit
import com.example.weathernow.presentation.favorites.FavoritesUiState
import com.example.weathernow.presentation.favorites.FavoritesViewModel
import com.example.weathernow.presentation.home.HomeUiState
import com.example.weathernow.presentation.home.HomeViewModel
import com.example.weathernow.presentation.search.SearchViewModel
import com.example.weathernow.presentation.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaselineViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun homeViewModel_initialState_isSuccessWithWeatherData() = runTest {
        val viewModel = HomeViewModel()
        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)
        val success = state as HomeUiState.Success
        assertEquals("Hanoi", success.location.name)
        assertEquals("Vietnam", success.location.country)
        assertEquals(28.0, success.currentWeather.temperatureCelsius, 0.01)
        assertTrue(success.hourlyForecast.isNotEmpty())
        assertTrue(success.dailyForecast.isNotEmpty())
    }

    @Test
    fun searchViewModel_queryUpdate_and_clearQuery() = runTest {
        val viewModel = SearchViewModel()
        assertEquals("", viewModel.uiState.value.query)

        viewModel.onQueryChange("Tokyo")
        assertEquals("Tokyo", viewModel.uiState.value.query)

        viewModel.clearQuery()
        assertEquals("", viewModel.uiState.value.query)
    }

    @Test
    fun favoritesViewModel_initialState_hasCurrentAndSavedCities() = runTest {
        val viewModel = FavoritesViewModel()
        val state = viewModel.uiState.value
        assertTrue(state is FavoritesUiState.Success)
        val success = state as FavoritesUiState.Success
        assertNotNull(success.currentLocation)
        assertEquals("Hanoi", success.currentLocation?.location?.name)
        assertEquals(4, success.favoritesList.size)
    }

    @Test
    fun settingsViewModel_updateTheme_units_and_language() = runTest {
        val viewModel = SettingsViewModel()
        viewModel.setTheme(AppTheme.DARK)
        viewModel.setLanguage(AppLanguage.VIETNAMESE)
        viewModel.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)

        val state = viewModel.uiState.value
        assertEquals(AppTheme.DARK, state.preferences.theme)
        assertEquals(AppLanguage.VIETNAMESE, state.preferences.language)
        assertEquals(TemperatureUnit.FAHRENHEIT, state.preferences.temperatureUnit)
    }

    @Test
    fun settingsViewModel_clearCache_resetsCacheSize() = runTest {
        val viewModel = SettingsViewModel()
        viewModel.clearOfflineCache()

        val state = viewModel.uiState.value
        assertEquals("0 KB", state.cachedDataSize)
    }
}

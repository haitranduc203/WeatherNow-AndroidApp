package com.example.weathernow.presentation

import app.cash.turbine.test
import com.example.weathernow.domain.model.AppTheme
import com.example.weathernow.domain.model.TemperatureUnit
import com.example.weathernow.domain.model.WeatherLocation
import com.example.weathernow.presentation.favorites.FavoritesUiState
import com.example.weathernow.presentation.favorites.FavoritesViewModel
import com.example.weathernow.presentation.home.HomeUiState
import com.example.weathernow.presentation.home.HomeViewModel
import com.example.weathernow.presentation.search.SearchUiState
import com.example.weathernow.presentation.search.SearchViewModel
import com.example.weathernow.presentation.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
    fun homeViewModel_initialState_isSuccessWithWeatherData() = runTest {
        val viewModel = HomeViewModel()
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is HomeUiState.Success)
            val success = state as HomeUiState.Success
            assertEquals("Hanoi", success.location.name)
            assertEquals("Vietnam", success.location.country)
            assertEquals(28.0, success.currentWeather.temperatureCelsius, 0.01)
            assertTrue(success.hourlyForecast.isNotEmpty())
            assertTrue(success.dailyForecast.isNotEmpty())
        }
    }

    @Test
    fun searchViewModel_queryUpdate_and_clearQuery() = runTest {
        val viewModel = SearchViewModel()
        viewModel.uiState.test {
            val initial = awaitItem()
            assertTrue(initial is SearchUiState.Content)

            viewModel.onQueryChange("Tokyo")
            val updated = awaitItem() as SearchUiState.Content
            assertEquals("Tokyo", updated.query)

            viewModel.clearQuery()
            val cleared = awaitItem() as SearchUiState.Content
            assertEquals("", cleared.query)
        }
    }

    @Test
    fun searchViewModel_toggleFavorite_updatesLocationFavoriteState() = runTest {
        val viewModel = SearchViewModel()
        val target = WeatherLocation(id = "4", name = "Haiphong", country = "Vietnam", latitude = 20.8449, longitude = 106.6881, isFavorite = false)

        viewModel.toggleFavorite(target)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as SearchUiState.Content
        val item = state.searchResults.firstOrNull { it.id == target.id }
        assertNotNull(item)
        assertTrue(item!!.isFavorite)
    }

    @Test
    fun favoritesViewModel_initialState_hasCurrentAndSavedCities() = runTest {
        val viewModel = FavoritesViewModel()
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is FavoritesUiState.Success)
            val success = state as FavoritesUiState.Success
            assertNotNull(success.currentLocation)
            assertEquals("Hanoi", success.currentLocation?.location?.name)
            assertEquals(4, success.favoritesList.size)
        }
    }

    @Test
    fun settingsViewModel_updateTheme_and_units() = runTest {
        val viewModel = SettingsViewModel()
        viewModel.setTheme(AppTheme.DARK)
        viewModel.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)

        val state = viewModel.uiState.value
        assertEquals(AppTheme.DARK, state.preferences.theme)
        assertEquals(TemperatureUnit.FAHRENHEIT, state.preferences.temperatureUnit)
    }

    @Test
    fun settingsViewModel_clearCache_resetsCacheSize() = runTest {
        val viewModel = SettingsViewModel()
        viewModel.clearOfflineCache()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("0 KB", state.cachedDataSize)
    }
}

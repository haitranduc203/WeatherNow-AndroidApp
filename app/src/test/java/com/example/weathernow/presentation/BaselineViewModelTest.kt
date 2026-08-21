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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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

    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val mockRemoteDataSource = object : com.example.weathernow.data.remote.datasource.OpenMeteoRemoteDataSource {
        override suspend fun getForecast(
            latitude: Double,
            longitude: Double,
            timezone: String,
            forecastDays: Int
        ): com.example.weathernow.data.remote.dto.OpenMeteoForecastDto {
            return com.example.weathernow.core.network.NetworkModule.json.decodeFromString<com.example.weathernow.data.remote.dto.OpenMeteoForecastDto>(com.example.weathernow.data.TestFixtures.FORECAST_JSON)
        }

        override suspend fun searchLocations(
            name: String,
            count: Int,
            language: String
        ): com.example.weathernow.data.remote.dto.OpenMeteoGeocodingDto {
            return com.example.weathernow.core.network.NetworkModule.json.decodeFromString<com.example.weathernow.data.remote.dto.OpenMeteoGeocodingDto>(com.example.weathernow.data.TestFixtures.GEOCODING_JSON)
        }
    }

    private fun createMockWeatherRepo() = com.example.weathernow.data.repository.WeatherRepositoryImpl(mockRemoteDataSource)
    private fun createMockLocationRepo() = com.example.weathernow.data.repository.LocationRepositoryImpl(mockRemoteDataSource)

    @Test
    fun homeViewModel_initialState_isSuccessWithWeatherData() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(weatherRepository = createMockWeatherRepo())
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue("Actual state was: $state", state is HomeUiState.Success)
        val success = state as HomeUiState.Success
        assertEquals("Hà Nội", success.location.name)
        assertEquals(28.4, success.currentWeather.temperatureCelsius, 0.01)
        assertTrue(success.hourlyForecast.isNotEmpty())
        assertTrue(success.dailyForecast.isNotEmpty())
    }

    @Test
    fun searchViewModel_queryUpdate_and_clearQuery() = runTest(testDispatcher) {
        val viewModel = SearchViewModel(locationRepository = createMockLocationRepo(), weatherRepository = createMockWeatherRepo())
        advanceUntilIdle()
        assertEquals("", viewModel.uiState.value.query)

        viewModel.onQueryChange("Tokyo")
        assertEquals("Tokyo", viewModel.uiState.value.query)

        viewModel.clearQuery()
        assertEquals("", viewModel.uiState.value.query)
    }

    @Test
    fun favoritesViewModel_initialState_hasCurrentAndSavedCities() = runTest(testDispatcher) {
        val viewModel = FavoritesViewModel(weatherRepository = createMockWeatherRepo())
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue("Actual state was: $state", state is FavoritesUiState.Success)
        val success = state as FavoritesUiState.Success
        assertNotNull(success.currentLocation)
        assertEquals("Hà Nội", success.currentLocation?.location?.name)
        assertEquals(3, success.favoritesList.size)
    }

    @Test
    fun settingsViewModel_updateTheme_units_and_language() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel()
        advanceUntilIdle()
        viewModel.setTheme(AppTheme.DARK)
        viewModel.setLanguage(AppLanguage.VIETNAMESE)
        viewModel.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AppTheme.DARK, state.preferences.theme)
        assertEquals(AppLanguage.VIETNAMESE, state.preferences.language)
        assertEquals(TemperatureUnit.FAHRENHEIT, state.preferences.temperatureUnit)
    }

    @Test
    fun settingsViewModel_clearCache_resetsCacheSize() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel()
        advanceUntilIdle()
        viewModel.clearOfflineCache()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("0 KB", state.cachedDataSize)
    }
}

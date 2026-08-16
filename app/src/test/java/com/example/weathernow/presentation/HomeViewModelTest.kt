package com.example.weathernow.presentation

import com.example.weathernow.core.common.Resource
import com.example.weathernow.domain.model.CurrentWeather
import com.example.weathernow.domain.model.DailyForecast
import com.example.weathernow.domain.model.HourlyForecast
import com.example.weathernow.domain.model.WeatherCondition
import com.example.weathernow.domain.model.WeatherLocation
import com.example.weathernow.domain.repository.WeatherRepository
import com.example.weathernow.presentation.home.HomeUiState
import com.example.weathernow.presentation.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val sampleCurrentWeather = CurrentWeather(
        temperatureCelsius = 28.5,
        feelsLikeCelsius = 31.0,
        humidityPercent = 65,
        windSpeedKmh = 12.0,
        windDirectionDegrees = 90,
        uvIndex = 6.0,
        pressureHpa = 1012.0,
        precipitationMm = 0.0,
        condition = WeatherCondition.PARTLY_CLOUDY,
        isDay = true,
        observedAt = Instant.parse("2026-08-16T12:00:00Z")
    )

    private val fakeWeatherRepository = object : WeatherRepository {
        override fun observeCurrentWeather(latitude: Double, longitude: Double): Flow<Resource<CurrentWeather>> {
            return flowOf(Resource.Success(sampleCurrentWeather))
        }

        override fun observeHourlyForecast(latitude: Double, longitude: Double): Flow<Resource<List<HourlyForecast>>> {
            return flowOf(Resource.Success(emptyList()))
        }

        override fun observeDailyForecast(latitude: Double, longitude: Double): Flow<Resource<List<DailyForecast>>> {
            return flowOf(Resource.Success(emptyList()))
        }

        override suspend fun refreshWeather(latitude: Double, longitude: Double): Resource<Unit> {
            return Resource.Success(Unit)
        }

        override fun observeFavoriteLocations(): Flow<List<WeatherLocation>> {
            return flowOf(
                listOf(
                    WeatherLocation("loc_1", "Tokyo", "Nhật Bản", "Tokyo", 35.6762, 139.6503),
                    WeatherLocation("loc_2", "Paris", "Pháp", "Île-de-France", 48.8566, 2.3522)
                )
            )
        }

        override suspend fun addFavoriteLocation(location: WeatherLocation): Resource<Unit> = Resource.Success(Unit)
        override suspend fun removeFavoriteLocation(locationId: String): Resource<Unit> = Resource.Success(Unit)
        override suspend fun isFavoriteLocation(latitude: Double, longitude: Double): Boolean = false
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialLoadWeatherData_Success() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(fakeWeatherRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)
        val successState = state as HomeUiState.Success
        assertEquals("Hà Nội", successState.location.name)
        assertEquals(28.5, successState.currentWeather.temperatureCelsius, 0.01)
    }

    @Test
    fun testSwitchLocationToParis_Success() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(fakeWeatherRepository)
        advanceUntilIdle()

        viewModel.loadWeatherData(
            latitude = 48.8566,
            longitude = 2.3522,
            locationName = "Paris",
            adminArea = "Île-de-France",
            country = "Pháp"
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)
        val successState = state as HomeUiState.Success
        assertEquals("Paris", successState.location.name)
        assertEquals("Pháp", successState.location.country)
        assertEquals(48.8566, successState.location.latitude, 0.001)
    }

    @Test
    fun testRefreshWeatherData_Success() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(fakeWeatherRepository)
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)
    }
}

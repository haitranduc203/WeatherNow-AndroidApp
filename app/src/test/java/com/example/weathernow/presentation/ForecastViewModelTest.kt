package com.example.weathernow.presentation

import com.example.weathernow.core.common.Resource
import com.example.weathernow.domain.model.CurrentWeather
import com.example.weathernow.domain.model.DailyForecast
import com.example.weathernow.domain.model.HourlyForecast
import com.example.weathernow.domain.model.WeatherCondition
import com.example.weathernow.domain.model.WeatherLocation
import com.example.weathernow.domain.repository.WeatherRepository
import com.example.weathernow.presentation.forecast.ForecastViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ForecastViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val sampleCurrent = CurrentWeather(
        temperatureCelsius = 30.0,
        feelsLikeCelsius = 34.0,
        humidityPercent = 70,
        windSpeedKmh = 14.0,
        windDirectionDegrees = 120,
        uvIndex = 7.0,
        pressureHpa = 1010.0,
        precipitationMm = 0.0,
        condition = WeatherCondition.CLEAR,
        isDay = true,
        observedAt = Instant.parse("2026-08-16T12:00:00Z")
    )

    private val sampleHourly = (0..5).map {
        HourlyForecast(
            time = Instant.parse("2026-08-16T00:00:00Z").plusSeconds(it * 3600L),
            temperatureCelsius = 26.0 + it,
            precipitationProbabilityPercent = 20,
            condition = WeatherCondition.CLEAR,
            isDay = true
        )
    }

    private val sampleDaily = listOf(
        DailyForecast(
            date = LocalDate.parse("2026-08-16"),
            minTemperatureCelsius = 25.0,
            maxTemperatureCelsius = 33.0,
            precipitationProbabilityPercent = 30,
            sunrise = Instant.parse("2026-08-16T05:30:00Z"),
            sunset = Instant.parse("2026-08-16T18:30:00Z"),
            condition = WeatherCondition.CLEAR
        )
    )

    private val fakeWeatherRepo = object : WeatherRepository {
        override fun observeCurrentWeather(latitude: Double, longitude: Double) = flowOf(Resource.Success(sampleCurrent))
        override fun observeHourlyForecast(latitude: Double, longitude: Double) = flowOf(Resource.Success(sampleHourly))
        override fun observeDailyForecast(latitude: Double, longitude: Double) = flowOf(Resource.Success(sampleDaily))
        override suspend fun refreshWeather(latitude: Double, longitude: Double) = Resource.Success(Unit)
        override fun observeFavoriteLocations() = flowOf<List<WeatherLocation>>()
        override suspend fun addFavoriteLocation(location: WeatherLocation) = Resource.Success(Unit)
        override suspend fun removeFavoriteLocation(locationId: String) = Resource.Success(Unit)
        override suspend fun isFavoriteLocation(latitude: Double, longitude: Double, name: String?) = false
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
    fun testLoadForecast_Success() = runTest(testDispatcher) {
        val viewModel = ForecastViewModel(fakeWeatherRepo)
        advanceUntilIdle()

        viewModel.loadForecast(35.6762, 139.6503, "Tokyo", "Tokyo")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Tokyo", state.locationName)
        assertEquals(34.0, state.feelsLikeCelsius, 0.01)
        assertEquals(6, state.hourlyList.size)
        assertEquals(1, state.dailyList.size)
    }
}

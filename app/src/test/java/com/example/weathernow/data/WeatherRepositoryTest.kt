package com.example.weathernow.data

import com.example.weathernow.core.common.Resource
import com.example.weathernow.core.network.NetworkModule
import com.example.weathernow.data.remote.datasource.OpenMeteoRemoteDataSource
import com.example.weathernow.data.remote.dto.OpenMeteoForecastDto
import com.example.weathernow.data.remote.dto.OpenMeteoGeocodingDto
import com.example.weathernow.data.repository.LocationRepositoryImpl
import com.example.weathernow.data.repository.WeatherRepositoryImpl
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherRepositoryTest {

    private val json = NetworkModule.json

    private val mockRemoteDataSource = object : OpenMeteoRemoteDataSource {
        override suspend fun getForecast(
            latitude: Double,
            longitude: Double,
            timezone: String,
            forecastDays: Int
        ): OpenMeteoForecastDto {
            return json.decodeFromString(TestFixtures.FORECAST_JSON)
        }

        override suspend fun searchLocations(
            name: String,
            count: Int,
            language: String
        ): OpenMeteoGeocodingDto {
            return json.decodeFromString(TestFixtures.GEOCODING_JSON)
        }
    }

    @Test
    fun `observeCurrentWeather emits Loading then Success with live data`() = runTest {
        val repository = WeatherRepositoryImpl(remoteDataSource = mockRemoteDataSource)
        val emissions = repository.observeCurrentWeather(21.0285, 105.8542).toList()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Success)

        val currentWeather = (emissions[1] as Resource.Success).data
        assertEquals(28.4, currentWeather.temperatureCelsius, 0.01)
    }

    @Test
    fun `observeHourlyForecast emits Loading then Success with 3 items`() = runTest {
        val repository = WeatherRepositoryImpl(remoteDataSource = mockRemoteDataSource)
        val emissions = repository.observeHourlyForecast(21.0285, 105.8542).toList()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Success)

        val hourly = (emissions[1] as Resource.Success).data
        assertEquals(3, hourly.size)
    }

    @Test
    fun `observeDailyForecast emits Loading then Success with 2 items`() = runTest {
        val repository = WeatherRepositoryImpl(remoteDataSource = mockRemoteDataSource)
        val emissions = repository.observeDailyForecast(21.0285, 105.8542).toList()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Success)

        val daily = (emissions[1] as Resource.Success).data
        assertEquals(2, daily.size)
    }

    @Test
    fun `LocationRepository searchLocations returns matching locations`() = runTest {
        val repository = LocationRepositoryImpl(remoteDataSource = mockRemoteDataSource)
        val result = repository.searchLocations("Hanoi")

        assertTrue(result is Resource.Success)
        val locations = (result as Resource.Success).data
        assertTrue(locations.isNotEmpty())
        assertEquals("Hà Nội", locations[0].name)
    }
}

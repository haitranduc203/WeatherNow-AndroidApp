package com.example.weathernow.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.weathernow.core.common.Resource
import com.example.weathernow.data.local.db.WeatherDatabase
import com.example.weathernow.data.local.db.entity.CachedWeatherEntity
import com.example.weathernow.data.remote.datasource.OpenMeteoRemoteDataSource
import com.example.weathernow.data.remote.dto.OpenMeteoForecastDto
import com.example.weathernow.data.remote.dto.OpenMeteoGeocodingDto
import com.example.weathernow.data.repository.WeatherRepositoryImpl
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ErrorHandlingAndResilienceTest {

    private lateinit var database: WeatherDatabase

    private val failingRemoteDataSource = object : OpenMeteoRemoteDataSource {
        override suspend fun getForecast(
            latitude: Double,
            longitude: Double,
            timezone: String,
            forecastDays: Int
        ): OpenMeteoForecastDto {
            throw IOException("Failed to connect to Open-Meteo backend (500 Server Error)")
        }

        override suspend fun searchLocations(name: String, count: Int, language: String): OpenMeteoGeocodingDto {
            throw IOException("Geocoding failed")
        }
    }

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testNetworkFailure_WithCache_FallsBackToCache() = runTest {
        val sampleEntity = CachedWeatherEntity(
            locationKey = "21.03_105.85",
            latitude = 21.0285,
            longitude = 105.8542,
            cityName = "Hà Nội",
            temperatureCelsius = 22.0,
            feelsLikeCelsius = 24.0,
            conditionName = "CLEAR",
            isDay = true,
            observedAtEpochMillis = 1700000000000L,
            hourlyJson = "[]",
            dailyJson = "[]",
            fetchedAtEpochMillis = 1700000000000L
        )

        database.cachedWeatherDao().insertOrReplace(sampleEntity)

        val repository = WeatherRepositoryImpl(
            remoteDataSource = failingRemoteDataSource,
            cachedWeatherDao = database.cachedWeatherDao(),
            favoriteLocationDao = database.favoriteLocationDao()
        )

        val result = repository.observeCurrentWeather(21.0285, 105.8542)
            .filter { it !is Resource.Loading }
            .first()

        assertTrue(result is Resource.Success)
        val data = (result as Resource.Success).data
        assertEquals(22.0, data.temperatureCelsius, 0.01)
    }

    @Test
    fun testNetworkFailure_WithoutCache_ReturnsError() = runTest {
        val repository = WeatherRepositoryImpl(
            remoteDataSource = failingRemoteDataSource,
            cachedWeatherDao = database.cachedWeatherDao(),
            favoriteLocationDao = database.favoriteLocationDao()
        )

        val result = repository.observeCurrentWeather(21.0285, 105.8542)
            .filter { it !is Resource.Loading }
            .first()

        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message.contains("Failed to connect"))
    }
}

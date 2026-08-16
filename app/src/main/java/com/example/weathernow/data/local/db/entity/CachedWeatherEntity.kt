package com.example.weathernow.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores cached weather forecast data per location to enable instant offline-first display.
 */
@Entity(tableName = "cached_weather")
data class CachedWeatherEntity(
    @PrimaryKey val locationKey: String, // e.g. "21.03_105.85"
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val country: String? = null,
    val adminArea: String? = null,
    val temperatureCelsius: Double,
    val feelsLikeCelsius: Double,
    val humidityPercent: Int? = null,
    val windSpeedKmh: Double? = null,
    val windDirectionDegrees: Int? = null,
    val uvIndex: Double? = null,
    val precipitationMm: Double? = null,
    val pressureHpa: Double? = null,
    val conditionName: String,
    val isDay: Boolean = true,
    val observedAtEpochMillis: Long,
    val hourlyJson: String, // JSON-encoded List<HourlyForecast>
    val dailyJson: String,  // JSON-encoded List<DailyForecast>
    val fetchedAtEpochMillis: Long = System.currentTimeMillis()
)

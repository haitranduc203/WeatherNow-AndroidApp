package com.example.weathernow.domain.model

import java.time.Instant

data class CurrentWeather(
    val temperatureCelsius: Double,
    val feelsLikeCelsius: Double,
    val humidityPercent: Int?,
    val windSpeedKmh: Double?,
    val windDirectionDegrees: Int? = null,
    val uvIndex: Double? = null,
    val precipitationMm: Double? = null,
    val pressureHpa: Double? = null,
    val condition: WeatherCondition,
    val observedAt: Instant
)

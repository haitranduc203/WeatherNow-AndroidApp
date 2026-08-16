package com.example.weathernow.domain.model

import java.time.Instant

data class CurrentWeather(
    val temperatureCelsius: Double,
    val feelsLikeCelsius: Double,
    val humidityPercent: Int?,
    val windSpeedKmh: Double?,
    val precipitationMm: Double?,
    val condition: WeatherCondition,
    val observedAt: Instant
)

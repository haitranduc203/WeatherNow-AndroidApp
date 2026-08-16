package com.example.weathernow.domain.model

import java.time.Instant

data class HourlyForecast(
    val time: Instant,
    val temperatureCelsius: Double,
    val precipitationProbabilityPercent: Int?,
    val condition: WeatherCondition,
    val isDay: Boolean = true
)

package com.example.weathernow.domain.model

import java.time.Instant
import java.time.LocalDate

data class DailyForecast(
    val date: LocalDate,
    val minTemperatureCelsius: Double,
    val maxTemperatureCelsius: Double,
    val precipitationProbabilityPercent: Int?,
    val sunrise: Instant?,
    val sunset: Instant?,
    val condition: WeatherCondition
)

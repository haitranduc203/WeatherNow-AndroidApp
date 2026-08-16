package com.example.weathernow.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoForecastDto(
    val latitude: Double,
    val longitude: Double,
    @SerialName("generationtime_ms") val generationTimeMs: Double? = null,
    @SerialName("utc_offset_seconds") val utcOffsetSeconds: Int? = null,
    val timezone: String? = null,
    @SerialName("timezone_abbreviation") val timezoneAbbreviation: String? = null,
    val elevation: Double? = null,
    val current: OpenMeteoCurrentDto? = null,
    val hourly: OpenMeteoHourlyDto? = null,
    val daily: OpenMeteoDailyDto? = null
)

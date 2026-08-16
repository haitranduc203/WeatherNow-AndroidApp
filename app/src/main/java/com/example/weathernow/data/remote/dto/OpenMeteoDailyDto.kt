package com.example.weathernow.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoDailyDto(
    val time: List<String> = emptyList(),
    @SerialName("weather_code") val weatherCode: List<Int> = emptyList(),
    @SerialName("temperature_2m_max") val temperature2mMax: List<Double> = emptyList(),
    @SerialName("temperature_2m_min") val temperature2mMin: List<Double> = emptyList(),
    @SerialName("apparent_temperature_max") val apparentTemperatureMax: List<Double?> = emptyList(),
    @SerialName("apparent_temperature_min") val apparentTemperatureMin: List<Double?> = emptyList(),
    @SerialName("precipitation_probability_max") val precipitationProbabilityMax: List<Int?> = emptyList(),
    @SerialName("precipitation_sum") val precipitationSum: List<Double?> = emptyList(),
    val sunrise: List<String> = emptyList(),
    val sunset: List<String> = emptyList(),
    @SerialName("uv_index_max") val uvIndexMax: List<Double?> = emptyList(),
    @SerialName("wind_speed_10m_max") val windSpeed10mMax: List<Double?> = emptyList()
)

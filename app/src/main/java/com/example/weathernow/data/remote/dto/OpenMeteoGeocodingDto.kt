package com.example.weathernow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoGeocodingDto(
    val results: List<OpenMeteoLocationDto>? = null,
    val generationtime_ms: Double? = null
)

package com.example.weathernow.domain.model

data class WeatherLocation(
    val id: String?,
    val name: String,
    val country: String?,
    val adminArea: String? = null,
    val latitude: Double,
    val longitude: Double,
    val timezone: String? = null,
    val isFavorite: Boolean = false
)

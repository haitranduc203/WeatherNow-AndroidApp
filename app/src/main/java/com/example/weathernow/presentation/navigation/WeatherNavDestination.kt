package com.example.weathernow.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface WeatherNavDestination : NavKey {
    @Serializable
    data object Onboarding : WeatherNavDestination
    
    @Serializable
    data object Home : WeatherNavDestination
    
    @Serializable
    data object Search : WeatherNavDestination
    
    @Serializable
    data object Favorites : WeatherNavDestination
    
    @Serializable
    data object Settings : WeatherNavDestination
    
    @Serializable
    data class ForecastDetail(
        val latitude: Double,
        val longitude: Double,
        val locationName: String,
        val adminArea: String? = null
    ) : WeatherNavDestination
}

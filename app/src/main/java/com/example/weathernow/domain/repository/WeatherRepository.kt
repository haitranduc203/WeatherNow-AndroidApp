package com.example.weathernow.domain.repository

import com.example.weathernow.core.common.Resource
import com.example.weathernow.domain.model.CurrentWeather
import com.example.weathernow.domain.model.DailyForecast
import com.example.weathernow.domain.model.HourlyForecast
import com.example.weathernow.domain.model.WeatherLocation
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun observeCurrentWeather(latitude: Double, longitude: Double): Flow<Resource<CurrentWeather>>
    fun observeHourlyForecast(latitude: Double, longitude: Double): Flow<Resource<List<HourlyForecast>>>
    fun observeDailyForecast(latitude: Double, longitude: Double): Flow<Resource<List<DailyForecast>>>
    suspend fun refreshWeather(latitude: Double, longitude: Double): Resource<Unit>
    
    fun observeFavoriteLocations(): Flow<List<WeatherLocation>>
    suspend fun addFavoriteLocation(location: WeatherLocation): Resource<Unit>
    suspend fun removeFavoriteLocation(locationId: String): Resource<Unit>
    suspend fun isFavoriteLocation(latitude: Double, longitude: Double, name: String? = null): Boolean
}

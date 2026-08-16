package com.example.weathernow.domain.repository

import com.example.weathernow.core.common.Resource
import com.example.weathernow.domain.model.WeatherLocation

import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun searchLocations(query: String): Resource<List<WeatherLocation>>
    suspend fun getCurrentDeviceLocation(): Resource<WeatherLocation>
    fun observeRecentSearches(limit: Int = 10): Flow<List<WeatherLocation>>
    suspend fun saveRecentSearch(location: WeatherLocation)
    suspend fun deleteRecentSearch(id: String)
    suspend fun clearRecentSearches()
    fun isLocationFavorite(latitude: Double, longitude: Double): Flow<Boolean>
    suspend fun toggleFavorite(location: WeatherLocation): Boolean
}

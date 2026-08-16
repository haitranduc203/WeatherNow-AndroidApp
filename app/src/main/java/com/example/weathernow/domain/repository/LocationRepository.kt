package com.example.weathernow.domain.repository

import com.example.weathernow.core.common.Resource
import com.example.weathernow.domain.model.WeatherLocation

interface LocationRepository {
    suspend fun searchLocations(query: String): Resource<List<WeatherLocation>>
    suspend fun getCurrentDeviceLocation(): Resource<WeatherLocation>
}

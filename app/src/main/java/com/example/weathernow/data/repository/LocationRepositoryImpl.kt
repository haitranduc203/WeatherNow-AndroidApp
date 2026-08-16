package com.example.weathernow.data.repository

import com.example.weathernow.core.common.Resource
import com.example.weathernow.data.mapper.LocationDtoMapper
import com.example.weathernow.data.remote.datasource.OpenMeteoRemoteDataSource
import com.example.weathernow.data.remote.datasource.OpenMeteoRemoteDataSourceImpl
import com.example.weathernow.domain.model.WeatherLocation
import com.example.weathernow.domain.repository.LocationRepository

class LocationRepositoryImpl(
    private val remoteDataSource: OpenMeteoRemoteDataSource = OpenMeteoRemoteDataSourceImpl()
) : LocationRepository {

    override suspend fun searchLocations(query: String): Resource<List<WeatherLocation>> {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return Resource.Success(emptyList())
        }

        return try {
            val response = remoteDataSource.searchLocations(name = trimmed, count = 10)
            val locations = LocationDtoMapper.mapListToDomain(response.results)
            Resource.Success(locations)
        } catch (e: Exception) {
            Resource.Error(message = e.message ?: "Failed to search locations", cause = e)
        }
    }

    override suspend fun getCurrentDeviceLocation(): Resource<WeatherLocation> {
        // Default fallback location for Hanoi, Vietnam
        return Resource.Success(
            WeatherLocation(
                id = "hanoi",
                name = "Hanoi",
                country = "Vietnam",
                adminArea = "Ha Noi",
                latitude = 21.0285,
                longitude = 105.8542,
                timezone = "Asia/Ho_Chi_Minh",
                isFavorite = false
            )
        )
    }
}

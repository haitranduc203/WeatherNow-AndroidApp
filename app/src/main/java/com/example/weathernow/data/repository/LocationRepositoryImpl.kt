package com.example.weathernow.data.repository

import com.example.weathernow.core.common.Resource
import com.example.weathernow.data.local.VietnamLocationsCatalog
import com.example.weathernow.data.mapper.LocationDtoMapper
import com.example.weathernow.data.remote.datasource.OpenMeteoRemoteDataSource
import com.example.weathernow.data.remote.datasource.OpenMeteoRemoteDataSourceImpl
import com.example.weathernow.domain.model.WeatherLocation
import com.example.weathernow.domain.repository.LocationRepository
import kotlin.math.abs

class LocationRepositoryImpl(
    private val remoteDataSource: OpenMeteoRemoteDataSource = OpenMeteoRemoteDataSourceImpl()
) : LocationRepository {

    override suspend fun searchLocations(query: String): Resource<List<WeatherLocation>> {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return Resource.Success(emptyList())
        }

        // 1. Search in curated Vietnam Locations Catalog (34 administrative units + key urban centers)
        val localMatches = VietnamLocationsCatalog.search(trimmed)

        // 2. Fetch from Open-Meteo Geocoding API for global/remote results
        return try {
            val response = remoteDataSource.searchLocations(name = trimmed, count = 10)
            val remoteLocations = LocationDtoMapper.mapListToDomain(response.results)

            // Filter out remote results that are duplicates or inaccurate villages in Vietnam
            val filteredRemote = remoteLocations.filter { remote ->
                val isDuplicateOfLocal = localMatches.any { local ->
                    (abs(local.latitude - remote.latitude) < 0.2 && abs(local.longitude - remote.longitude) < 0.2) ||
                    (local.name.equals(remote.name, ignoreCase = true) && remote.country?.contains("Vietnam", ignoreCase = true) == true)
                }
                !isDuplicateOfLocal
            }

            // Merge local matches first, followed by international / distinct remote matches
            val combined = (localMatches + filteredRemote).distinctBy { loc ->
                "${loc.name}_${loc.country}_${String.format(java.util.Locale.US, "%.1f", loc.latitude)}_${String.format(java.util.Locale.US, "%.1f", loc.longitude)}"
            }

            Resource.Success(combined)
        } catch (e: Exception) {
            // If network fails but we have local matches, return local matches gracefully
            if (localMatches.isNotEmpty()) {
                Resource.Success(localMatches)
            } else {
                Resource.Error(message = e.message ?: "Failed to search locations", cause = e)
            }
        }
    }

    override suspend fun getCurrentDeviceLocation(): Resource<WeatherLocation> {
        // Default location for Hanoi, Vietnam
        val hanoiEntry = VietnamLocationsCatalog.entries.firstOrNull { it.id == "vn_hanoi" }
        val location = hanoiEntry?.toWeatherLocation() ?: WeatherLocation(
            id = "vn_hanoi",
            name = "Hà Nội",
            country = "Việt Nam",
            adminArea = "Thủ đô Hà Nội",
            latitude = 21.0285,
            longitude = 105.8542,
            timezone = "Asia/Ho_Chi_Minh",
            isFavorite = false
        )
        return Resource.Success(location)
    }
}

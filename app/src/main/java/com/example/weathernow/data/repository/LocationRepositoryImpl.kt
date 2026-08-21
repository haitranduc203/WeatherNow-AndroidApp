package com.example.weathernow.data.repository

import com.example.weathernow.core.common.Resource
import com.example.weathernow.data.local.VietnamLocationsCatalog
import com.example.weathernow.data.mapper.LocationDtoMapper
import com.example.weathernow.data.remote.datasource.OpenMeteoRemoteDataSource
import com.example.weathernow.data.remote.datasource.OpenMeteoRemoteDataSourceImpl
import com.example.weathernow.domain.model.WeatherLocation
import com.example.weathernow.domain.repository.LocationRepository
import kotlin.math.abs

import com.example.weathernow.data.local.db.WeatherDatabase
import com.example.weathernow.data.local.db.dao.FavoriteLocationDao
import com.example.weathernow.data.local.db.dao.RecentSearchDao
import com.example.weathernow.data.local.db.entity.FavoriteLocationEntity
import com.example.weathernow.data.local.db.entity.RecentSearchEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class LocationRepositoryImpl(
    private val remoteDataSource: OpenMeteoRemoteDataSource = OpenMeteoRemoteDataSourceImpl(),
    favoriteLocationDao: FavoriteLocationDao? = null,
    recentSearchDao: RecentSearchDao? = null
) : LocationRepository {

    private val favoriteLocationDao: FavoriteLocationDao? = favoriteLocationDao
        get() = field ?: WeatherDatabase.getInstanceOrNull()?.favoriteLocationDao()

    private val recentSearchDao: RecentSearchDao? = recentSearchDao
        get() = field ?: WeatherDatabase.getInstanceOrNull()?.recentSearchDao()

    private val fallbackRecentSearches = MutableStateFlow<List<WeatherLocation>>(
        listOf(
            WeatherLocation("vn_hanoi", "Hà Nội", "Việt Nam", "Thủ đô Hà Nội", 21.0285, 105.8542, "Asia/Ho_Chi_Minh"),
            WeatherLocation("tokyo", "Tokyo", "Japan", "Tokyo", 35.6762, 139.6503, "Asia/Tokyo"),
            WeatherLocation("paris", "Paris", "France", "Île-de-France", 48.8566, 2.3522, "Europe/Paris")
        )
    )

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
            }.map { loc ->
                val isFav = favoriteLocationDao?.isFavoriteSync(loc.latitude, loc.longitude) ?: loc.isFavorite
                loc.copy(isFavorite = isFav)
            }

            Resource.Success(combined)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // If network fails but we have local matches, return local matches gracefully
            if (localMatches.isNotEmpty()) {
                val markedLocal = localMatches.map { loc ->
                    val isFav = favoriteLocationDao?.isFavoriteSync(loc.latitude, loc.longitude) ?: loc.isFavorite
                    loc.copy(isFavorite = isFav)
                }
                Resource.Success(markedLocal)
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

    override fun observeRecentSearches(limit: Int): Flow<List<WeatherLocation>> {
        val dao = recentSearchDao ?: return fallbackRecentSearches.asStateFlow()
        return dao.observeRecentSearches(limit).map { entities ->
            if (entities.isEmpty()) {
                fallbackRecentSearches.value
            } else {
                entities.map { entity ->
                    WeatherLocation(
                        id = entity.id,
                        name = entity.name,
                        country = entity.country,
                        adminArea = entity.adminArea,
                        latitude = entity.latitude,
                        longitude = entity.longitude,
                        isFavorite = favoriteLocationDao?.isFavoriteSync(entity.latitude, entity.longitude) ?: false
                    )
                }
            }
        }
    }

    override suspend fun saveRecentSearch(location: WeatherLocation) {
        val dao = recentSearchDao
        if (dao != null) {
            val key = "${String.format(java.util.Locale.US, "%.2f", location.latitude)}_${String.format(java.util.Locale.US, "%.2f", location.longitude)}"
            dao.insertSearch(
                RecentSearchEntity(
                    id = key,
                    name = location.name,
                    country = location.country,
                    adminArea = location.adminArea,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    searchedAt = System.currentTimeMillis()
                )
            )
        } else {
            val current = fallbackRecentSearches.value.toMutableList()
            current.removeAll { it.name.equals(location.name, ignoreCase = true) }
            current.add(0, location)
            fallbackRecentSearches.value = current.take(10)
        }
    }

    override suspend fun deleteRecentSearch(id: String) {
        val dao = recentSearchDao
        if (dao != null) {
            dao.deleteSearchById(id)
        } else {
            val current = fallbackRecentSearches.value.toMutableList()
            current.removeAll { it.id == id || it.name.equals(id, ignoreCase = true) }
            fallbackRecentSearches.value = current
        }
    }

    override suspend fun clearRecentSearches() {
        val dao = recentSearchDao
        if (dao != null) {
            dao.clearRecentSearches()
        } else {
            fallbackRecentSearches.value = emptyList()
        }
    }

    override fun isLocationFavorite(latitude: Double, longitude: Double): Flow<Boolean> {
        val dao = favoriteLocationDao ?: return flowOf(false)
        return dao.isFavorite(latitude, longitude)
    }

    override suspend fun toggleFavorite(location: WeatherLocation): Boolean {
        val dao = favoriteLocationDao ?: return !location.isFavorite
        val currentlyFav = dao.isFavoriteSync(location.latitude, location.longitude)
        if (currentlyFav) {
            dao.deleteFavoriteByCoords(location.latitude, location.longitude)
            return false
        } else {
            val id = "${String.format(java.util.Locale.US, "%.2f", location.latitude)}_${String.format(java.util.Locale.US, "%.2f", location.longitude)}"
            dao.insertFavorite(
                FavoriteLocationEntity(
                    id = id,
                    name = location.name,
                    country = location.country,
                    adminArea = location.adminArea,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timezone = location.timezone,
                    isPinned = false,
                    createdAt = System.currentTimeMillis()
                )
            )
            return true
        }
    }
}

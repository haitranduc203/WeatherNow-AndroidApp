package com.example.weathernow.data.repository

import com.example.weathernow.core.common.Resource
import com.example.weathernow.data.mapper.ForecastDtoMapper
import com.example.weathernow.data.remote.datasource.OpenMeteoRemoteDataSource
import com.example.weathernow.data.remote.datasource.OpenMeteoRemoteDataSourceImpl
import com.example.weathernow.data.remote.dto.OpenMeteoForecastDto
import com.example.weathernow.domain.model.CurrentWeather
import com.example.weathernow.domain.model.DailyForecast
import com.example.weathernow.domain.model.HourlyForecast
import com.example.weathernow.domain.model.WeatherLocation
import com.example.weathernow.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

import com.example.weathernow.data.local.db.WeatherDatabase
import com.example.weathernow.data.local.db.converter.WeatherCacheSerializer
import com.example.weathernow.data.local.db.dao.CachedWeatherDao
import com.example.weathernow.data.local.db.dao.FavoriteLocationDao
import com.example.weathernow.data.local.db.entity.CachedWeatherEntity
import com.example.weathernow.data.local.db.entity.FavoriteLocationEntity
import com.example.weathernow.domain.model.WeatherCondition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant

class WeatherRepositoryImpl(
    private val remoteDataSource: OpenMeteoRemoteDataSource = OpenMeteoRemoteDataSourceImpl(),
    cachedWeatherDao: CachedWeatherDao? = null,
    favoriteLocationDao: FavoriteLocationDao? = null
) : WeatherRepository {

    private val cachedWeatherDao: CachedWeatherDao? = cachedWeatherDao
        get() = field ?: WeatherDatabase.getInstanceOrNull()?.cachedWeatherDao()

    private val favoriteLocationDao: FavoriteLocationDao? = favoriteLocationDao
        get() = field ?: WeatherDatabase.getInstanceOrNull()?.favoriteLocationDao()

    private val defaultStarterFavorites = listOf(
        WeatherLocation("vn_hanoi", "Hà Nội", "Việt Nam", "Thủ đô Hà Nội", 21.0285, 105.8542, "Asia/Ho_Chi_Minh", true),
        WeatherLocation("tokyo", "Tokyo", "Japan", "Tokyo", 35.6762, 139.6503, "Asia/Tokyo", true),
        WeatherLocation("paris", "Paris", "France", "Île-de-France", 48.8566, 2.3522, "Europe/Paris", true),
        WeatherLocation("newyork", "New York", "USA", "New York", 40.7128, -74.0060, "America/New_York", true)
    )

    private val _fallbackFavorites = MutableStateFlow<List<WeatherLocation>>(defaultStarterFavorites)
    private var initialFavoritesSeeded = false

    private fun locationKey(latitude: Double, longitude: Double): String {
        return "${String.format(java.util.Locale.US, "%.2f", latitude)}_${String.format(java.util.Locale.US, "%.2f", longitude)}"
    }

    private suspend fun saveForecastToCache(
        latitude: Double,
        longitude: Double,
        dto: OpenMeteoForecastDto
    ) {
        val dao = cachedWeatherDao ?: return
        try {
            val current = ForecastDtoMapper.mapToCurrentWeather(dto)
            val hourly = ForecastDtoMapper.mapToHourlyForecast(dto)
            val daily = ForecastDtoMapper.mapToDailyForecast(dto)

            val key = locationKey(latitude, longitude)
            val entity = CachedWeatherEntity(
                locationKey = key,
                latitude = latitude,
                longitude = longitude,
                cityName = "",
                country = null,
                adminArea = null,
                temperatureCelsius = current.temperatureCelsius,
                feelsLikeCelsius = current.feelsLikeCelsius,
                humidityPercent = current.humidityPercent,
                windSpeedKmh = current.windSpeedKmh,
                windDirectionDegrees = current.windDirectionDegrees,
                uvIndex = current.uvIndex,
                precipitationMm = current.precipitationMm,
                pressureHpa = current.pressureHpa,
                conditionName = current.condition.name,
                isDay = current.isDay,
                observedAtEpochMillis = current.observedAt.toEpochMilli(),
                hourlyJson = WeatherCacheSerializer.encodeHourly(hourly),
                dailyJson = WeatherCacheSerializer.encodeDaily(daily),
                fetchedAtEpochMillis = System.currentTimeMillis()
            )
            dao.insertOrReplace(entity)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            // Ignore cache save errors
        }
    }

    override fun observeCurrentWeather(latitude: Double, longitude: Double): Flow<Resource<CurrentWeather>> = flow {
        val key = locationKey(latitude, longitude)
        var hasCachedData = false

        // 1. Emit cached data immediately if available (0ms offline-first)
        cachedWeatherDao?.getCachedWeather(key)?.let { cached ->
            val condition = try {
                WeatherCondition.valueOf(cached.conditionName)
            } catch (_: Exception) {
                WeatherCondition.UNKNOWN
            }
            val cachedWeather = CurrentWeather(
                temperatureCelsius = cached.temperatureCelsius,
                feelsLikeCelsius = cached.feelsLikeCelsius,
                humidityPercent = cached.humidityPercent,
                windSpeedKmh = cached.windSpeedKmh,
                windDirectionDegrees = cached.windDirectionDegrees,
                uvIndex = cached.uvIndex,
                precipitationMm = cached.precipitationMm,
                pressureHpa = cached.pressureHpa,
                condition = condition,
                observedAt = Instant.ofEpochMilli(cached.observedAtEpochMillis),
                isDay = cached.isDay
            )
            hasCachedData = true
            emit(Resource.Success(cachedWeather))
        }

        if (!hasCachedData) {
            emit(Resource.Loading)
        }

        // 2. Fetch fresh data from remote API and update Room cache
        try {
            val dto = remoteDataSource.getForecast(latitude = latitude, longitude = longitude)
            val current = ForecastDtoMapper.mapToCurrentWeather(dto)
            saveForecastToCache(latitude, longitude, dto)
            emit(Resource.Success(current))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            if (!hasCachedData) {
                emit(Resource.Error(message = e.message ?: "Failed to load current weather", cause = e))
            }
        }
    }

    override fun observeHourlyForecast(latitude: Double, longitude: Double): Flow<Resource<List<HourlyForecast>>> = flow {
        val key = locationKey(latitude, longitude)
        var hasCachedData = false

        // 1. Emit cached hourly data immediately
        cachedWeatherDao?.getCachedWeather(key)?.let { cached ->
            val decoded = WeatherCacheSerializer.decodeHourly(cached.hourlyJson)
            if (decoded.isNotEmpty()) {
                hasCachedData = true
                emit(Resource.Success(decoded))
            }
        }

        if (!hasCachedData) {
            emit(Resource.Loading)
        }

        // 2. Fetch fresh hourly forecast
        try {
            val dto = remoteDataSource.getForecast(latitude = latitude, longitude = longitude)
            val hourly = ForecastDtoMapper.mapToHourlyForecast(dto)
            saveForecastToCache(latitude, longitude, dto)
            emit(Resource.Success(hourly))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            if (!hasCachedData) {
                emit(Resource.Error(message = e.message ?: "Failed to load hourly forecast", cause = e))
            }
        }
    }

    override fun observeDailyForecast(latitude: Double, longitude: Double): Flow<Resource<List<DailyForecast>>> = flow {
        val key = locationKey(latitude, longitude)
        var hasCachedData = false

        // 1. Emit cached daily data immediately
        cachedWeatherDao?.getCachedWeather(key)?.let { cached ->
            val decoded = WeatherCacheSerializer.decodeDaily(cached.dailyJson)
            if (decoded.isNotEmpty()) {
                hasCachedData = true
                emit(Resource.Success(decoded))
            }
        }

        if (!hasCachedData) {
            emit(Resource.Loading)
        }

        // 2. Fetch fresh daily forecast
        try {
            val dto = remoteDataSource.getForecast(latitude = latitude, longitude = longitude)
            val daily = ForecastDtoMapper.mapToDailyForecast(dto)
            saveForecastToCache(latitude, longitude, dto)
            emit(Resource.Success(daily))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            if (!hasCachedData) {
                emit(Resource.Error(message = e.message ?: "Failed to load daily forecast", cause = e))
            }
        }
    }

    override suspend fun refreshWeather(latitude: Double, longitude: Double): Resource<Unit> {
        return try {
            val dto = remoteDataSource.getForecast(latitude = latitude, longitude = longitude)
            saveForecastToCache(latitude, longitude, dto)
            Resource.Success(Unit)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Resource.Error(message = e.message ?: "Failed to refresh weather", cause = e)
        }
    }

    override fun observeFavoriteLocations(): Flow<List<WeatherLocation>> = flow {
        val dao = favoriteLocationDao
        if (dao == null) {
            emitAll(_fallbackFavorites.asStateFlow())
        } else {
            emitAll(
                dao.observeFavorites().map { entities ->
                    if (entities.isEmpty() && !initialFavoritesSeeded) {
                        initialFavoritesSeeded = true
                        defaultStarterFavorites.forEach { loc ->
                            dao.insertFavorite(
                                FavoriteLocationEntity(
                                    id = loc.id ?: "${loc.latitude}_${loc.longitude}",
                                    name = loc.name,
                                    country = loc.country,
                                    adminArea = loc.adminArea,
                                    latitude = loc.latitude,
                                    longitude = loc.longitude,
                                    timezone = loc.timezone,
                                    isPinned = false,
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                        defaultStarterFavorites
                    } else {
                        entities.map { entity ->
                            WeatherLocation(
                                id = entity.id,
                                name = entity.name,
                                country = entity.country,
                                adminArea = entity.adminArea,
                                latitude = entity.latitude,
                                longitude = entity.longitude,
                                timezone = entity.timezone,
                                isFavorite = true
                            )
                        }
                    }
                }.flowOn(Dispatchers.IO)
            )
        }
    }

    override suspend fun addFavoriteLocation(location: WeatherLocation): Resource<Unit> = withContext(Dispatchers.IO) {
        val dao = favoriteLocationDao
        if (dao != null) {
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
        } else {
            val currentList = _fallbackFavorites.value.toMutableList()
            val exists = currentList.any { it.name.equals(location.name, ignoreCase = true) || (it.latitude == location.latitude && it.longitude == location.longitude) }
            if (!exists) {
                currentList.add(location.copy(isFavorite = true))
                _fallbackFavorites.value = currentList
            }
        }
        Resource.Success(Unit)
    }

    override suspend fun removeFavoriteLocation(locationId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        val dao = favoriteLocationDao
        if (dao != null) {
            val deleted = dao.deleteFavoriteById(locationId)
            if (deleted == 0) {
                dao.deleteFavoriteByName(locationId)
            }
        } else {
            val currentList = _fallbackFavorites.value.toMutableList()
            currentList.removeAll { it.id == locationId || it.name.equals(locationId, ignoreCase = true) }
            _fallbackFavorites.value = currentList
        }
        Resource.Success(Unit)
    }

    override suspend fun isFavoriteLocation(latitude: Double, longitude: Double, name: String?): Boolean = withContext(Dispatchers.IO) {
        val dao = favoriteLocationDao
        if (dao != null) {
            if (!name.isNullOrBlank()) {
                dao.isFavoriteLocation(latitude, longitude, name)
            } else {
                dao.isFavoriteSync(latitude, longitude)
            }
        } else {
            _fallbackFavorites.value.any {
                (it.latitude == latitude && it.longitude == longitude) ||
                (!name.isNullOrBlank() && it.name.equals(name, ignoreCase = true))
            }
        }
    }
}

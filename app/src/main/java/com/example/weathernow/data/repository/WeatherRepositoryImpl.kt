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

class WeatherRepositoryImpl(
    private val remoteDataSource: OpenMeteoRemoteDataSource = OpenMeteoRemoteDataSourceImpl()
) : WeatherRepository {

    private val _favoritesFlow = MutableStateFlow<List<WeatherLocation>>(
        listOf(
            WeatherLocation("tokyo", "Tokyo", "Japan", "Tokyo", 35.6762, 139.6503, "Asia/Tokyo", true),
            WeatherLocation("paris", "Paris", "France", "Île-de-France", 48.8566, 2.3522, "Europe/Paris", true),
            WeatherLocation("newyork", "New York", "USA", "New York", 40.7128, -74.0060, "America/New_York", true),
            WeatherLocation("sydney", "Sydney", "Australia", "New South Wales", -33.8688, 151.2093, "Australia/Sydney", true)
        )
    )

    private var cachedDto: Pair<Pair<Double, Double>, OpenMeteoForecastDto>? = null

    private suspend fun getOrFetchDto(latitude: Double, longitude: Double): OpenMeteoForecastDto {
        val cached = cachedDto
        if (cached != null && cached.first.first == latitude && cached.first.second == longitude) {
            return cached.second
        }
        val dto = remoteDataSource.getForecast(latitude = latitude, longitude = longitude)
        cachedDto = Pair(Pair(latitude, longitude), dto)
        return dto
    }

    override fun observeCurrentWeather(latitude: Double, longitude: Double): Flow<Resource<CurrentWeather>> = flow {
        emit(Resource.Loading)
        try {
            val dto = getOrFetchDto(latitude, longitude)
            val current = ForecastDtoMapper.mapToCurrentWeather(dto)
            emit(Resource.Success(current))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Failed to load current weather", cause = e))
        }
    }

    override fun observeHourlyForecast(latitude: Double, longitude: Double): Flow<Resource<List<HourlyForecast>>> = flow {
        emit(Resource.Loading)
        try {
            val dto = getOrFetchDto(latitude, longitude)
            val hourly = ForecastDtoMapper.mapToHourlyForecast(dto)
            emit(Resource.Success(hourly))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Failed to load hourly forecast", cause = e))
        }
    }

    override fun observeDailyForecast(latitude: Double, longitude: Double): Flow<Resource<List<DailyForecast>>> = flow {
        emit(Resource.Loading)
        try {
            val dto = getOrFetchDto(latitude, longitude)
            val daily = ForecastDtoMapper.mapToDailyForecast(dto)
            emit(Resource.Success(daily))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Failed to load daily forecast", cause = e))
        }
    }

    override suspend fun refreshWeather(latitude: Double, longitude: Double): Resource<Unit> {
        return try {
            cachedDto = null
            val dto = remoteDataSource.getForecast(latitude = latitude, longitude = longitude)
            cachedDto = Pair(Pair(latitude, longitude), dto)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(message = e.message ?: "Failed to refresh weather", cause = e)
        }
    }

    override fun observeFavoriteLocations(): Flow<List<WeatherLocation>> = _favoritesFlow.asStateFlow()

    override suspend fun addFavoriteLocation(location: WeatherLocation): Resource<Unit> {
        val currentList = _favoritesFlow.value.toMutableList()
        val exists = currentList.any { it.name.equals(location.name, ignoreCase = true) || (it.latitude == location.latitude && it.longitude == location.longitude) }
        if (!exists) {
            currentList.add(location.copy(isFavorite = true))
            _favoritesFlow.value = currentList
        }
        return Resource.Success(Unit)
    }

    override suspend fun removeFavoriteLocation(locationId: String): Resource<Unit> {
        val currentList = _favoritesFlow.value.toMutableList()
        currentList.removeAll { it.id == locationId || it.name.equals(locationId, ignoreCase = true) }
        _favoritesFlow.value = currentList
        return Resource.Success(Unit)
    }

    override suspend fun isFavoriteLocation(latitude: Double, longitude: Double): Boolean {
        return _favoritesFlow.value.any { it.latitude == latitude && it.longitude == longitude }
    }
}

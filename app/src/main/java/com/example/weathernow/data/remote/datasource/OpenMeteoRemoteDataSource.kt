package com.example.weathernow.data.remote.datasource

import com.example.weathernow.core.network.NetworkModule
import com.example.weathernow.data.mapper.ErrorMapper
import com.example.weathernow.data.remote.api.OpenMeteoForecastApi
import com.example.weathernow.data.remote.api.OpenMeteoGeocodingApi
import com.example.weathernow.data.remote.dto.OpenMeteoForecastDto
import com.example.weathernow.data.remote.dto.OpenMeteoGeocodingDto

interface OpenMeteoRemoteDataSource {
    suspend fun getForecast(
        latitude: Double,
        longitude: Double,
        timezone: String = "auto",
        forecastDays: Int = 7
    ): OpenMeteoForecastDto

    suspend fun searchLocations(
        name: String,
        count: Int = 10,
        language: String = "en"
    ): OpenMeteoGeocodingDto
}

class OpenMeteoRemoteDataSourceImpl(
    private val forecastApi: OpenMeteoForecastApi = NetworkModule.forecastRetrofit.create(OpenMeteoForecastApi::class.java),
    private val geocodingApi: OpenMeteoGeocodingApi = NetworkModule.geocodingRetrofit.create(OpenMeteoGeocodingApi::class.java)
) : OpenMeteoRemoteDataSource {

    override suspend fun getForecast(
        latitude: Double,
        longitude: Double,
        timezone: String,
        forecastDays: Int
    ): OpenMeteoForecastDto {
        return try {
            forecastApi.getForecast(
                latitude = latitude,
                longitude = longitude,
                timezone = timezone,
                forecastDays = forecastDays
            )
        } catch (e: Throwable) {
            throw ErrorMapper.mapThrowableToNetworkError(e)
        }
    }

    override suspend fun searchLocations(
        name: String,
        count: Int,
        language: String
    ): OpenMeteoGeocodingDto {
        return try {
            geocodingApi.searchLocations(
                name = name,
                count = count,
                language = language
            )
        } catch (e: Throwable) {
            throw ErrorMapper.mapThrowableToNetworkError(e)
        }
    }
}

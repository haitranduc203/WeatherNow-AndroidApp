package com.example.weathernow.core.di

import android.content.Context
import com.example.weathernow.data.local.db.WeatherDatabase
import com.example.weathernow.data.remote.datasource.OpenMeteoRemoteDataSource
import com.example.weathernow.data.repository.LocationRepositoryImpl
import com.example.weathernow.data.repository.WeatherRepositoryImpl
import com.example.weathernow.domain.repository.LocationRepository
import com.example.weathernow.domain.repository.WeatherRepository

interface AppContainer {
    val database: WeatherDatabase
    val weatherRepository: WeatherRepository
    val locationRepository: LocationRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: WeatherDatabase by lazy {
        WeatherDatabase.getInstance(context)
    }

    private val remoteDataSource: OpenMeteoRemoteDataSource by lazy {
        com.example.weathernow.data.remote.datasource.OpenMeteoRemoteDataSourceImpl()
    }

    override val weatherRepository: WeatherRepository by lazy {
        WeatherRepositoryImpl(
            remoteDataSource = remoteDataSource,
            cachedWeatherDao = database.cachedWeatherDao()
        )
    }

    override val locationRepository: LocationRepository by lazy {
        LocationRepositoryImpl(
            remoteDataSource = remoteDataSource,
            favoriteLocationDao = database.favoriteLocationDao(),
            recentSearchDao = database.recentSearchDao()
        )
    }
}

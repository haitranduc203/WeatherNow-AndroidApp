package com.example.weathernow.core.di

import android.content.Context
import com.example.weathernow.data.local.db.WeatherDatabase
import com.example.weathernow.data.remote.datasource.OpenMeteoRemoteDataSource
import com.example.weathernow.data.repository.LocationRepositoryImpl
import com.example.weathernow.data.repository.WeatherRepositoryImpl
import com.example.weathernow.domain.repository.LocationRepository
import com.example.weathernow.domain.repository.WeatherRepository

import com.example.weathernow.data.local.datastore.UserPreferencesDataStore
import com.example.weathernow.data.local.datastore.userPreferencesDataStore
import com.example.weathernow.data.repository.UserPreferencesRepositoryImpl
import com.example.weathernow.domain.repository.UserPreferencesRepository

interface AppContainer {
    val database: WeatherDatabase
    val weatherRepository: WeatherRepository
    val locationRepository: LocationRepository
    val userPreferencesRepository: UserPreferencesRepository
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
            cachedWeatherDao = database.cachedWeatherDao(),
            favoriteLocationDao = database.favoriteLocationDao()
        )
    }

    override val locationRepository: LocationRepository by lazy {
        LocationRepositoryImpl(
            remoteDataSource = remoteDataSource,
            favoriteLocationDao = database.favoriteLocationDao(),
            recentSearchDao = database.recentSearchDao()
        )
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        val dataStore = UserPreferencesDataStore(context.userPreferencesDataStore)
        UserPreferencesRepositoryImpl(dataStore)
    }
}

package com.example.weathernow.domain.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton holder that shares the currently active/selected location
 * between HomeScreen, FavoritesScreen, SearchScreen, and Background Sync.
 */
object ActiveLocationManager {
    val defaultLocation = WeatherLocation(
        id = "vn_hanoi",
        name = "Hà Nội",
        country = "Việt Nam",
        adminArea = "Thủ đô Hà Nội",
        latitude = 21.0285,
        longitude = 105.8542,
        timezone = "Asia/Ho_Chi_Minh",
        isFavorite = true
    )

    private val _activeLocation = MutableStateFlow(defaultLocation)
    val activeLocation: StateFlow<WeatherLocation> = _activeLocation.asStateFlow()

    fun setActiveLocation(location: WeatherLocation) {
        _activeLocation.value = location
    }

    fun getActiveLocation(): WeatherLocation = _activeLocation.value
}

package com.example.weathernow.core.common

object Constants {
    const val FORECAST_BASE_URL = "https://api.open-meteo.com/"
    const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
    
    const val DATABASE_NAME = "weather_now.db"
    const val PREFERENCES_NAME = "weather_preferences"
    
    const val CACHE_FRESHNESS_MINUTES = 30L
    const val SEARCH_DEBOUNCE_MILLIS = 400L
    
    const val WEATHER_WORK_NAME = "periodic_weather_refresh_work"
    const val WEATHER_NOTIFICATION_CHANNEL_ID = "weather_alerts_channel"
}

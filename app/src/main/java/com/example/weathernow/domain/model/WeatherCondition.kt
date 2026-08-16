package com.example.weathernow.domain.model

enum class WeatherCondition(val displayName: String) {
    CLEAR("Clear Sky"),
    PARTLY_CLOUDY("Partly Cloudy"),
    CLOUDY("Cloudy"),
    FOG("Foggy"),
    DRIZZLE("Drizzle"),
    RAIN("Rain"),
    SNOW("Snow"),
    THUNDERSTORM("Thunderstorm"),
    UNKNOWN("Unknown")
}

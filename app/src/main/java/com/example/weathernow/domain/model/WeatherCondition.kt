package com.example.weathernow.domain.model

enum class WeatherCondition(val displayNameEn: String, val displayNameVi: String) {
    CLEAR("Clear Sky", "Trời quang"),
    PARTLY_CLOUDY("Partly Cloudy", "Có mây rải rác"),
    CLOUDY("Cloudy", "Nhiều mây"),
    FOG("Foggy", "Sương mù"),
    DRIZZLE("Drizzle", "Mưa phùn"),
    RAIN("Rain", "Mưa rào"),
    SNOW("Snow", "Tuyết rơi"),
    THUNDERSTORM("Thunderstorm", "Giông bão"),
    UNKNOWN("Unknown", "Không xác định");

    val displayName: String get() = displayNameEn

    fun localizedName(language: AppLanguage): String {
        return when (language) {
            AppLanguage.VIETNAMESE -> displayNameVi
            AppLanguage.ENGLISH -> displayNameEn
        }
    }
}

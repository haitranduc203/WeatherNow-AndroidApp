package com.example.weathernow.data.mapper

import com.example.weathernow.domain.model.WeatherCondition

object WeatherCodeMapper {

    /**
     * Maps WMO Weather Interpretation Codes to the application's domain [WeatherCondition].
     * Reference: Open-Meteo WMO Code table.
     */
    fun mapCodeToCondition(code: Int?): WeatherCondition {
        return when (code) {
            0 -> WeatherCondition.CLEAR
            1, 2 -> WeatherCondition.PARTLY_CLOUDY
            3 -> WeatherCondition.CLOUDY
            45, 48 -> WeatherCondition.FOG
            51, 53, 55, 56, 57 -> WeatherCondition.DRIZZLE
            61, 63, 65, 66, 67, 80, 81, 82 -> WeatherCondition.RAIN
            71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOW
            95, 96, 99 -> WeatherCondition.THUNDERSTORM
            else -> WeatherCondition.UNKNOWN
        }
    }
}

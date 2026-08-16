package com.example.weathernow.presentation.util

import com.example.weathernow.domain.model.TemperatureUnit
import com.example.weathernow.domain.model.WindSpeedUnit
import kotlin.math.roundToInt

object WeatherUnitsFormatter {

    fun formatTemperature(
        celsius: Double,
        unit: TemperatureUnit = TemperatureUnit.CELSIUS,
        includeUnitSymbol: Boolean = true
    ): String {
        val converted = when (unit) {
            TemperatureUnit.CELSIUS -> celsius
            TemperatureUnit.FAHRENHEIT -> (celsius * 9.0 / 5.0) + 32.0
        }
        val rounded = converted.roundToInt()
        return if (includeUnitSymbol) {
            when (unit) {
                TemperatureUnit.CELSIUS -> "${rounded}°C"
                TemperatureUnit.FAHRENHEIT -> "${rounded}°F"
            }
        } else {
            "${rounded}°"
        }
    }

    fun formatWindSpeed(
        kmh: Double,
        unit: WindSpeedUnit = WindSpeedUnit.KMH
    ): String {
        return when (unit) {
            WindSpeedUnit.KMH -> "${kmh.roundToInt()} km/h"
            WindSpeedUnit.MPH -> "${(kmh * 0.621371).roundToInt()} mph"
            WindSpeedUnit.MS -> "${(kmh / 3.6).roundToInt()} m/s"
        }
    }
}

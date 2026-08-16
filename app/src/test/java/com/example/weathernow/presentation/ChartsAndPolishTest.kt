package com.example.weathernow.presentation

import com.example.weathernow.domain.model.HourlyForecast
import com.example.weathernow.domain.model.TemperatureUnit
import com.example.weathernow.domain.model.WeatherCondition
import com.example.weathernow.presentation.util.WeatherUnitsFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class ChartsAndPolishTest {

    private val sampleHourlyList = (0..23).map { i ->
        HourlyForecast(
            time = Instant.parse("2026-08-16T00:00:00Z").plus(i.toLong(), ChronoUnit.HOURS),
            temperatureCelsius = 25.0 + (i % 8) * 1.5,
            precipitationProbabilityPercent = (i * 4) % 100,
            condition = if (i in 6..18) WeatherCondition.CLEAR else WeatherCondition.PARTLY_CLOUDY,
            isDay = i in 6..18
        )
    }

    @Test
    fun testChartMinMaxCalculations() {
        val temps = sampleHourlyList.map { it.temperatureCelsius }
        val minTemp = temps.minOrNull() ?: 0.0
        val maxTemp = temps.maxOrNull() ?: 0.0

        assertEquals(25.0, minTemp, 0.01)
        assertEquals(35.5, maxTemp, 0.01)
        assertTrue(maxTemp > minTemp)
    }

    @Test
    fun testChartTemperatureUnitConversion() {
        val minTemp = 25.0
        val maxTemp = 35.5

        val celsiusMin = WeatherUnitsFormatter.formatTemperature(minTemp, TemperatureUnit.CELSIUS)
        val fahrenheitMin = WeatherUnitsFormatter.formatTemperature(minTemp, TemperatureUnit.FAHRENHEIT)

        val celsiusMax = WeatherUnitsFormatter.formatTemperature(maxTemp, TemperatureUnit.CELSIUS)
        val fahrenheitMax = WeatherUnitsFormatter.formatTemperature(maxTemp, TemperatureUnit.FAHRENHEIT)

        assertEquals("25°C", celsiusMin)
        assertEquals("77°F", fahrenheitMin)
        assertEquals("36°C", celsiusMax)
        assertEquals("96°F", fahrenheitMax)
    }

    @Test
    fun testPrecipitationProbabilitiesClamping() {
        val probs = sampleHourlyList.mapNotNull { it.precipitationProbabilityPercent }
        assertTrue(probs.all { it in 0..100 })
    }
}

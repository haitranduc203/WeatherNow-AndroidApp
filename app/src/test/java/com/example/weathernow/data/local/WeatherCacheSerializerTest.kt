package com.example.weathernow.data.local

import com.example.weathernow.data.local.db.converter.WeatherCacheSerializer
import com.example.weathernow.domain.model.DailyForecast
import com.example.weathernow.domain.model.HourlyForecast
import com.example.weathernow.domain.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class WeatherCacheSerializerTest {

    @Test
    fun encodeAndDecodeHourlyForecast() {
        val hourly = listOf(
            HourlyForecast(
                time = Instant.ofEpochMilli(1700000000000L),
                temperatureCelsius = 28.5,
                precipitationProbabilityPercent = 15,
                condition = WeatherCondition.CLEAR,
                isDay = false
            ),
            HourlyForecast(
                time = Instant.ofEpochMilli(1700003600000L),
                temperatureCelsius = 27.0,
                precipitationProbabilityPercent = 20,
                condition = WeatherCondition.PARTLY_CLOUDY,
                isDay = true
            )
        )

        val json = WeatherCacheSerializer.encodeHourly(hourly)
        val decoded = WeatherCacheSerializer.decodeHourly(json)

        assertEquals(2, decoded.size)
        assertEquals(28.5, decoded[0].temperatureCelsius, 0.01)
        assertEquals(WeatherCondition.CLEAR, decoded[0].condition)
        assertEquals(false, decoded[0].isDay)
        assertEquals(WeatherCondition.PARTLY_CLOUDY, decoded[1].condition)
        assertEquals(true, decoded[1].isDay)
    }

    @Test
    fun encodeAndDecodeDailyForecast() {
        val daily = listOf(
            DailyForecast(
                date = LocalDate.of(2026, 8, 16),
                minTemperatureCelsius = 24.0,
                maxTemperatureCelsius = 34.0,
                precipitationProbabilityPercent = 40,
                sunrise = Instant.ofEpochMilli(1700000000000L),
                sunset = Instant.ofEpochMilli(1700040000000L),
                condition = WeatherCondition.RAIN
            )
        )

        val json = WeatherCacheSerializer.encodeDaily(daily)
        val decoded = WeatherCacheSerializer.decodeDaily(json)

        assertEquals(1, decoded.size)
        assertEquals(LocalDate.of(2026, 8, 16), decoded[0].date)
        assertEquals(24.0, decoded[0].minTemperatureCelsius, 0.01)
        assertEquals(34.0, decoded[0].maxTemperatureCelsius, 0.01)
        assertEquals(WeatherCondition.RAIN, decoded[0].condition)
        assertNotNull(decoded[0].sunrise)
        assertNotNull(decoded[0].sunset)
    }
}

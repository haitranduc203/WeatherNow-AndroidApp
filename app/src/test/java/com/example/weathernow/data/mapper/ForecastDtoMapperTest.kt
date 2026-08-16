package com.example.weathernow.data.mapper

import com.example.weathernow.core.network.NetworkModule
import com.example.weathernow.data.TestFixtures
import com.example.weathernow.data.remote.dto.OpenMeteoForecastDto
import com.example.weathernow.data.remote.dto.OpenMeteoHourlyDto
import com.example.weathernow.domain.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class ForecastDtoMapperTest {

    private val json = NetworkModule.json

    @Test
    fun `mapToCurrentWeather correctly parses and maps OpenMeteoForecastDto`() {
        val dto = json.decodeFromString<OpenMeteoForecastDto>(TestFixtures.FORECAST_JSON)
        val currentWeather = ForecastDtoMapper.mapToCurrentWeather(dto)

        assertEquals(28.4, currentWeather.temperatureCelsius, 0.01)
        assertEquals(31.2, currentWeather.feelsLikeCelsius, 0.01)
        assertEquals(68, currentWeather.humidityPercent)
        assertEquals(12.5, currentWeather.windSpeedKmh!!, 0.01)
        assertEquals(45, currentWeather.windDirectionDegrees)
        assertEquals(0.0, currentWeather.precipitationMm!!, 0.01)
        assertEquals(1012.8, currentWeather.pressureHpa!!, 0.01)
        assertEquals(5.2, currentWeather.uvIndex!!, 0.01)
        assertEquals(WeatherCondition.PARTLY_CLOUDY, currentWeather.condition)
        assertNotNull(currentWeather.observedAt)
    }

    @Test
    fun `mapToHourlyForecast correctly maps parallel arrays`() {
        val dto = json.decodeFromString<OpenMeteoForecastDto>(TestFixtures.FORECAST_JSON)
        val hourlyList = ForecastDtoMapper.mapToHourlyForecast(dto)

        assertEquals(3, hourlyList.size)
        assertEquals(28.0, hourlyList[0].temperatureCelsius, 0.01)
        assertEquals(0, hourlyList[0].precipitationProbabilityPercent)
        assertEquals(WeatherCondition.PARTLY_CLOUDY, hourlyList[0].condition)

        assertEquals(29.5, hourlyList[1].temperatureCelsius, 0.01)
        assertEquals(10, hourlyList[1].precipitationProbabilityPercent)
        assertEquals(WeatherCondition.PARTLY_CLOUDY, hourlyList[1].condition)

        assertEquals(31.0, hourlyList[2].temperatureCelsius, 0.01)
        assertEquals(20, hourlyList[2].precipitationProbabilityPercent)
        assertEquals(WeatherCondition.CLOUDY, hourlyList[2].condition)
    }

    @Test
    fun `mapToDailyForecast correctly maps 7-day parallel arrays`() {
        val dto = json.decodeFromString<OpenMeteoForecastDto>(TestFixtures.FORECAST_JSON)
        val dailyList = ForecastDtoMapper.mapToDailyForecast(dto)

        assertEquals(2, dailyList.size)
        assertEquals(LocalDate.of(2026, 8, 16), dailyList[0].date)
        assertEquals(25.0, dailyList[0].minTemperatureCelsius, 0.01)
        assertEquals(33.5, dailyList[0].maxTemperatureCelsius, 0.01)
        assertEquals(20, dailyList[0].precipitationProbabilityPercent)
        assertEquals(WeatherCondition.PARTLY_CLOUDY, dailyList[0].condition)
        assertNotNull(dailyList[0].sunrise)
        assertNotNull(dailyList[0].sunset)

        assertEquals(LocalDate.of(2026, 8, 17), dailyList[1].date)
        assertEquals(24.5, dailyList[1].minTemperatureCelsius, 0.01)
        assertEquals(30.0, dailyList[1].maxTemperatureCelsius, 0.01)
        assertEquals(80, dailyList[1].precipitationProbabilityPercent)
        assertEquals(WeatherCondition.RAIN, dailyList[1].condition)
    }

    @Test
    fun `mapToHourlyForecast handles uneven parallel arrays safely`() {
        val unevenDto = OpenMeteoForecastDto(
            latitude = 0.0,
            longitude = 0.0,
            hourly = OpenMeteoHourlyDto(
                time = listOf("2026-08-16T10:00", "2026-08-16T11:00"),
                temperature2m = listOf(25.0), // only 1 element
                weatherCode = listOf(0, 1)
            )
        )

        val result = ForecastDtoMapper.mapToHourlyForecast(unevenDto)
        assertEquals(1, result.size)
        assertEquals(25.0, result[0].temperatureCelsius, 0.01)
    }
}

package com.example.weathernow.data.mapper

import com.example.weathernow.data.remote.dto.OpenMeteoForecastDto
import com.example.weathernow.domain.model.CurrentWeather
import com.example.weathernow.domain.model.DailyForecast
import com.example.weathernow.domain.model.HourlyForecast
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

object ForecastDtoMapper {

    private fun resolveZoneId(dto: OpenMeteoForecastDto): ZoneId {
        return try {
            if (!dto.timezone.isNullOrBlank()) {
                ZoneId.of(dto.timezone)
            } else if (dto.utcOffsetSeconds != null) {
                ZoneOffset.ofTotalSeconds(dto.utcOffsetSeconds)
            } else {
                ZoneOffset.UTC
            }
        } catch (_: Exception) {
            if (dto.utcOffsetSeconds != null) {
                try {
                    ZoneOffset.ofTotalSeconds(dto.utcOffsetSeconds)
                } catch (_: Exception) {
                    ZoneOffset.UTC
                }
            } else {
                ZoneOffset.UTC
            }
        }
    }

    private fun parseTimeToInstant(timeStr: String, zoneId: ZoneId): Instant {
        return try {
            if (timeStr.contains("T")) {
                val ldt = LocalDateTime.parse(timeStr)
                ldt.atZone(zoneId).toInstant()
            } else {
                val ld = LocalDate.parse(timeStr)
                ld.atStartOfDay(zoneId).toInstant()
            }
        } catch (_: Exception) {
            Instant.now()
        }
    }

    private fun parseTimeToLocalDate(timeStr: String): LocalDate {
        return try {
            if (timeStr.contains("T")) {
                LocalDate.parse(timeStr.substringBefore("T"))
            } else {
                LocalDate.parse(timeStr)
            }
        } catch (_: Exception) {
            LocalDate.now()
        }
    }

    fun mapToCurrentWeather(dto: OpenMeteoForecastDto): CurrentWeather {
        val current = dto.current ?: throw IllegalStateException("Current weather payload is missing in Open-Meteo response")
        val zoneId = resolveZoneId(dto)
        val observedAt = parseTimeToInstant(current.time, zoneId)

        return CurrentWeather(
            temperatureCelsius = current.temperature2m,
            feelsLikeCelsius = current.apparentTemperature ?: current.temperature2m,
            humidityPercent = current.relativeHumidity2m,
            windSpeedKmh = current.windSpeed10m,
            windDirectionDegrees = current.windDirection10m,
            uvIndex = current.uvIndex,
            precipitationMm = current.precipitation,
            pressureHpa = current.surfacePressure,
            condition = WeatherCodeMapper.mapCodeToCondition(current.weatherCode),
            observedAt = observedAt,
            isDay = (current.isDay == 1) || (current.isDay == null && observedAt.atZone(zoneId).hour in 6..18)
        )
    }

    fun mapToHourlyForecast(dto: OpenMeteoForecastDto): List<HourlyForecast> {
        val hourly = dto.hourly ?: return emptyList()
        val zoneId = resolveZoneId(dto)

        val times = hourly.time
        val temps = hourly.temperature2m
        val probs = hourly.precipitationProbability
        val codes = hourly.weatherCode
        val isDayList = hourly.isDay

        val size = minOf(times.size, temps.size, codes.size)
        val result = ArrayList<HourlyForecast>(size)

        for (i in 0 until size) {
            val timeStr = times[i]
            val instant = parseTimeToInstant(timeStr, zoneId)
            val temp = temps[i]
            val prob = if (i < probs.size) probs[i] else null
            val code = codes[i]
            val isDay = if (i < isDayList.size && isDayList[i] != null) {
                isDayList[i] == 1
            } else {
                instant.atZone(zoneId).hour in 6..18
            }

            result.add(
                HourlyForecast(
                    time = instant,
                    temperatureCelsius = temp,
                    precipitationProbabilityPercent = prob,
                    condition = WeatherCodeMapper.mapCodeToCondition(code),
                    isDay = isDay
                )
            )
        }

        return result
    }

    fun mapToDailyForecast(dto: OpenMeteoForecastDto): List<DailyForecast> {
        val daily = dto.daily ?: return emptyList()
        val zoneId = resolveZoneId(dto)

        val times = daily.time
        val codes = daily.weatherCode
        val maxTemps = daily.temperature2mMax
        val minTemps = daily.temperature2mMin
        val probs = daily.precipitationProbabilityMax
        val sunrises = daily.sunrise
        val sunsets = daily.sunset

        val size = minOf(times.size, codes.size, maxTemps.size, minTemps.size)
        val result = ArrayList<DailyForecast>(size)

        for (i in 0 until size) {
            val date = parseTimeToLocalDate(times[i])
            val maxTemp = maxTemps[i]
            val minTemp = minTemps[i]
            val prob = if (i < probs.size) probs[i] else null
            val code = codes[i]
            val sunrise = if (i < sunrises.size && sunrises[i].isNotBlank()) parseTimeToInstant(sunrises[i], zoneId) else null
            val sunset = if (i < sunsets.size && sunsets[i].isNotBlank()) parseTimeToInstant(sunsets[i], zoneId) else null

            result.add(
                DailyForecast(
                    date = date,
                    minTemperatureCelsius = minTemp,
                    maxTemperatureCelsius = maxTemp,
                    precipitationProbabilityPercent = prob,
                    sunrise = sunrise,
                    sunset = sunset,
                    condition = WeatherCodeMapper.mapCodeToCondition(code)
                )
            )
        }

        return result
    }
}

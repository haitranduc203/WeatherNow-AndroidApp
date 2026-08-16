package com.example.weathernow.data.local.db.converter

import com.example.weathernow.domain.model.DailyForecast
import com.example.weathernow.domain.model.HourlyForecast
import com.example.weathernow.domain.model.WeatherCondition
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate

@Serializable
data class CachedHourlyItem(
    val timeEpochMillis: Long,
    val temperatureCelsius: Double,
    val precipitationProbabilityPercent: Int?,
    val conditionName: String,
    val isDay: Boolean = true
)

@Serializable
data class CachedDailyItem(
    val dateEpochDays: Long,
    val minTemperatureCelsius: Double,
    val maxTemperatureCelsius: Double,
    val precipitationProbabilityPercent: Int?,
    val sunriseEpochMillis: Long?,
    val sunsetEpochMillis: Long?,
    val conditionName: String
)

object WeatherCacheSerializer {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun encodeHourly(list: List<HourlyForecast>): String {
        val dtos = list.map {
            CachedHourlyItem(
                timeEpochMillis = it.time.toEpochMilli(),
                temperatureCelsius = it.temperatureCelsius,
                precipitationProbabilityPercent = it.precipitationProbabilityPercent,
                conditionName = it.condition.name,
                isDay = it.isDay
            )
        }
        return json.encodeToString(dtos)
    }

    fun decodeHourly(jsonStr: String): List<HourlyForecast> {
        if (jsonStr.isBlank()) return emptyList()
        return try {
            val dtos = json.decodeFromString<List<CachedHourlyItem>>(jsonStr)
            dtos.map {
                HourlyForecast(
                    time = Instant.ofEpochMilli(it.timeEpochMillis),
                    temperatureCelsius = it.temperatureCelsius,
                    precipitationProbabilityPercent = it.precipitationProbabilityPercent,
                    condition = try { WeatherCondition.valueOf(it.conditionName) } catch (_: Exception) { WeatherCondition.UNKNOWN },
                    isDay = it.isDay
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun encodeDaily(list: List<DailyForecast>): String {
        val dtos = list.map {
            CachedDailyItem(
                dateEpochDays = it.date.toEpochDay(),
                minTemperatureCelsius = it.minTemperatureCelsius,
                maxTemperatureCelsius = it.maxTemperatureCelsius,
                precipitationProbabilityPercent = it.precipitationProbabilityPercent,
                sunriseEpochMillis = it.sunrise?.toEpochMilli(),
                sunsetEpochMillis = it.sunset?.toEpochMilli(),
                conditionName = it.condition.name
            )
        }
        return json.encodeToString(dtos)
    }

    fun decodeDaily(jsonStr: String): List<DailyForecast> {
        if (jsonStr.isBlank()) return emptyList()
        return try {
            val dtos = json.decodeFromString<List<CachedDailyItem>>(jsonStr)
            dtos.map {
                DailyForecast(
                    date = LocalDate.ofEpochDay(it.dateEpochDays),
                    minTemperatureCelsius = it.minTemperatureCelsius,
                    maxTemperatureCelsius = it.maxTemperatureCelsius,
                    precipitationProbabilityPercent = it.precipitationProbabilityPercent,
                    sunrise = it.sunriseEpochMillis?.let { ms -> Instant.ofEpochMilli(ms) },
                    sunset = it.sunsetEpochMillis?.let { ms -> Instant.ofEpochMilli(ms) },
                    condition = try { WeatherCondition.valueOf(it.conditionName) } catch (_: Exception) { WeatherCondition.UNKNOWN }
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}

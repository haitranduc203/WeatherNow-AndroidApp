package com.example.weathernow.data.mapper

import com.example.weathernow.domain.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherCodeMapperTest {

    @Test
    fun `mapCodeToCondition maps code 0 to CLEAR`() {
        assertEquals(WeatherCondition.CLEAR, WeatherCodeMapper.mapCodeToCondition(0))
    }

    @Test
    fun `mapCodeToCondition maps codes 1 and 2 to PARTLY_CLOUDY`() {
        assertEquals(WeatherCondition.PARTLY_CLOUDY, WeatherCodeMapper.mapCodeToCondition(1))
        assertEquals(WeatherCondition.PARTLY_CLOUDY, WeatherCodeMapper.mapCodeToCondition(2))
    }

    @Test
    fun `mapCodeToCondition maps code 3 to CLOUDY`() {
        assertEquals(WeatherCondition.CLOUDY, WeatherCodeMapper.mapCodeToCondition(3))
    }

    @Test
    fun `mapCodeToCondition maps codes 45 and 48 to FOG`() {
        assertEquals(WeatherCondition.FOG, WeatherCodeMapper.mapCodeToCondition(45))
        assertEquals(WeatherCondition.FOG, WeatherCodeMapper.mapCodeToCondition(48))
    }

    @Test
    fun `mapCodeToCondition maps drizzle codes to DRIZZLE`() {
        listOf(51, 53, 55, 56, 57).forEach { code ->
            assertEquals(WeatherCondition.DRIZZLE, WeatherCodeMapper.mapCodeToCondition(code))
        }
    }

    @Test
    fun `mapCodeToCondition maps rain codes to RAIN`() {
        listOf(61, 63, 65, 66, 67, 80, 81, 82).forEach { code ->
            assertEquals(WeatherCondition.RAIN, WeatherCodeMapper.mapCodeToCondition(code))
        }
    }

    @Test
    fun `mapCodeToCondition maps snow codes to SNOW`() {
        listOf(71, 73, 75, 77, 85, 86).forEach { code ->
            assertEquals(WeatherCondition.SNOW, WeatherCodeMapper.mapCodeToCondition(code))
        }
    }

    @Test
    fun `mapCodeToCondition maps thunderstorm codes to THUNDERSTORM`() {
        listOf(95, 96, 99).forEach { code ->
            assertEquals(WeatherCondition.THUNDERSTORM, WeatherCodeMapper.mapCodeToCondition(code))
        }
    }

    @Test
    fun `mapCodeToCondition maps unknown codes to UNKNOWN`() {
        assertEquals(WeatherCondition.UNKNOWN, WeatherCodeMapper.mapCodeToCondition(-1))
        assertEquals(WeatherCondition.UNKNOWN, WeatherCodeMapper.mapCodeToCondition(999))
        assertEquals(WeatherCondition.UNKNOWN, WeatherCodeMapper.mapCodeToCondition(null))
    }
}

package com.example.weathernow.data.remote

import com.example.weathernow.core.network.NetworkModule
import com.example.weathernow.data.remote.dto.OpenMeteoForecastDto
import com.example.weathernow.data.remote.dto.OpenMeteoGeocodingDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class OpenMeteoJsonSerializationTest {

    @Test
    fun parseSampleForecastJson() {
        val jsonString = this::class.java.classLoader?.getResourceAsStream("sample_forecast.json")?.bufferedReader()?.use { it.readText() }
        assertNotNull("sample_forecast.json must exist in test resources", jsonString)

        val dto = NetworkModule.json.decodeFromString<OpenMeteoForecastDto>(jsonString!!)
        assertEquals(21.0285, dto.latitude, 0.001)
        assertEquals(105.8542, dto.longitude, 0.001)
        assertEquals("Asia/Bangkok", dto.timezone)
        assertNotNull(dto.current)
        assertEquals(34.2, dto.current?.temperature2m ?: 0.0, 0.01)
        assertEquals(66, dto.current?.relativeHumidity2m)
        assertEquals(1, dto.current?.isDay)
        assertNotNull(dto.hourly)
        assertEquals(4, dto.hourly?.time?.size)
        assertNotNull(dto.daily)
        assertEquals(2, dto.daily?.time?.size)
    }

    @Test
    fun parseSampleGeocodingJson() {
        val jsonString = this::class.java.classLoader?.getResourceAsStream("sample_geocoding.json")?.bufferedReader()?.use { it.readText() }
        assertNotNull("sample_geocoding.json must exist in test resources", jsonString)

        val dto = NetworkModule.json.decodeFromString<OpenMeteoGeocodingDto>(jsonString!!)
        assertNotNull(dto.results)
        assertEquals(2, dto.results?.size)
        assertEquals("Hà Nội", dto.results?.get(0)?.name)
        assertEquals("Thủ đô Hà Nội", dto.results?.get(0)?.admin1)
        assertEquals("Thái Bình", dto.results?.get(1)?.name)
    }
}

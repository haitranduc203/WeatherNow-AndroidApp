package com.example.weathernow.data.mapper

import com.example.weathernow.core.network.NetworkModule
import com.example.weathernow.data.TestFixtures
import com.example.weathernow.data.remote.dto.OpenMeteoGeocodingDto
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationDtoMapperTest {

    private val json = NetworkModule.json

    @Test
    fun `mapListToDomain maps Geocoding results to WeatherLocation list`() {
        val dto = json.decodeFromString<OpenMeteoGeocodingDto>(TestFixtures.GEOCODING_JSON)
        val locations = LocationDtoMapper.mapListToDomain(dto.results)

        assertEquals(2, locations.size)

        assertEquals("1581130", locations[0].id)
        assertEquals("Hanoi", locations[0].name)
        assertEquals("Vietnam", locations[0].country)
        assertEquals("Ha Noi", locations[0].adminArea)
        assertEquals(21.02817, locations[0].latitude, 0.0001)
        assertEquals(105.85417, locations[0].longitude, 0.0001)
        assertEquals("Asia/Ho_Chi_Minh", locations[0].timezone)

        assertEquals("1850147", locations[1].id)
        assertEquals("Tokyo", locations[1].name)
        assertEquals("Japan", locations[1].country)
        assertEquals("Tokyo", locations[1].adminArea)
        assertEquals(35.6895, locations[1].latitude, 0.0001)
        assertEquals(139.69171, locations[1].longitude, 0.0001)
        assertEquals("Asia/Tokyo", locations[1].timezone)
    }

    @Test
    fun `mapListToDomain handles null and empty results safely`() {
        assertEquals(0, LocationDtoMapper.mapListToDomain(null).size)
        assertEquals(0, LocationDtoMapper.mapListToDomain(emptyList()).size)
    }
}

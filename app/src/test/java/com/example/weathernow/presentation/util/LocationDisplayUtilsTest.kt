package com.example.weathernow.presentation.util

import com.example.weathernow.domain.model.WeatherLocation
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationDisplayUtilsTest {

    @Test
    fun deviceLocation_vietnamese_displaysViTriHienTai() {
        val loc = WeatherLocation(
            id = "device_10.8231_106.6297",
            name = "Current location",
            country = null,
            latitude = 10.8231,
            longitude = 106.6297
        )
        assertEquals("Vị trí hiện tại", loc.getDisplayName(VietnameseStrings))
        assertEquals("Vị trí hiện tại", getLocalizedLocationName(loc.name, loc.id, VietnameseStrings))
    }

    @Test
    fun deviceLocation_english_displaysCurrentLocation() {
        val loc = WeatherLocation(
            id = "device_10.8231_106.6297",
            name = "Current location",
            country = null,
            latitude = 10.8231,
            longitude = 106.6297
        )
        assertEquals("Current Location", loc.getDisplayName(EnglishStrings))
        assertEquals("Current Location", getLocalizedLocationName(loc.name, loc.id, EnglishStrings))
    }

    @Test
    fun legacyStoredLocation_nameCurrentLocation_localized() {
        val loc = WeatherLocation(
            id = "10.82_106.63",
            name = "Current location",
            country = null,
            latitude = 10.8231,
            longitude = 106.6297
        )
        assertEquals("Vị trí hiện tại", loc.getDisplayName(VietnameseStrings))
        assertEquals("Current Location", loc.getDisplayName(EnglishStrings))
        assertEquals("Vị trí hiện tại", getLocalizedLocationName(loc.name, loc.id, VietnameseStrings))
    }

    @Test
    fun normalLocation_tokyoAndParis_notTranslated() {
        val tokyo = WeatherLocation(id = "tokyo", name = "Tokyo", country = "Japan", latitude = 35.6762, longitude = 139.6503)
        val paris = WeatherLocation(id = "paris", name = "Paris", country = "France", latitude = 48.8566, longitude = 2.3522)

        assertEquals("Tokyo", tokyo.getDisplayName(VietnameseStrings))
        assertEquals("Tokyo", tokyo.getDisplayName(EnglishStrings))
        assertEquals("Paris", paris.getDisplayName(VietnameseStrings))
        assertEquals("Paris", paris.getDisplayName(EnglishStrings))
    }
}

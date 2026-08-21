package com.example.weathernow.presentation.util

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherStringsLocalizationTest {

    @Test
    fun vietnameseStrings_containRequiredLocalizationKeys() {
        assertEquals("Vị trí hiện tại", VietnameseStrings.currentPosition)
        assertEquals("Đang định vị…", VietnameseStrings.locating)
        assertEquals("Không thể tải dữ liệu thời tiết", VietnameseStrings.unableToLoadWeather)
        assertEquals("Cần cấp quyền vị trí để sử dụng tính năng này", VietnameseStrings.locationPermissionRequired)
        assertEquals("Không thể xác định vị trí. Hãy thử tìm kiếm thủ công.", VietnameseStrings.locationUnavailable)
        assertEquals("Không thể xác định vị trí. Vui lòng thử lại.", VietnameseStrings.genericLocationFailure)
        assertEquals("Không thể tìm kiếm địa điểm. Vui lòng thử lại.", VietnameseStrings.genericSearchFailure)
        assertEquals("Lỗi", VietnameseStrings.error)
        assertEquals("Thử lại", VietnameseStrings.retry)
        assertEquals("Tìm kiếm địa điểm", VietnameseStrings.searchLocation)
        assertEquals("Thử đồng bộ lại", VietnameseStrings.retrySync)
        assertEquals("Hướng gió: 299° Đông", VietnameseStrings.windDirectionLabel(299))
    }

    @Test
    fun englishStrings_containRequiredLocalizationKeys() {
        assertEquals("Current Location", EnglishStrings.currentPosition)
        assertEquals("Locating…", EnglishStrings.locating)
        assertEquals("Unable to Load Weather", EnglishStrings.unableToLoadWeather)
        assertEquals("Location permission is required to use this feature", EnglishStrings.locationPermissionRequired)
        assertEquals("Unable to obtain device location. Try searching manually.", EnglishStrings.locationUnavailable)
        assertEquals("Unable to obtain device location. Please try again.", EnglishStrings.genericLocationFailure)
        assertEquals("Unable to search locations. Please try again.", EnglishStrings.genericSearchFailure)
        assertEquals("Error", EnglishStrings.error)
        assertEquals("Retry", EnglishStrings.retry)
        assertEquals("Search Location", EnglishStrings.searchLocation)
        assertEquals("Retry Sync", EnglishStrings.retrySync)
        assertEquals("Direction: 299° East", EnglishStrings.windDirectionLabel(299))
    }
}

package com.example.weathernow.data

import com.example.weathernow.core.common.Resource
import com.example.weathernow.data.local.VietnamLocationsCatalog
import com.example.weathernow.data.location.DeviceCoordinates
import com.example.weathernow.data.location.DeviceLocationDataSource
import com.example.weathernow.data.remote.datasource.OpenMeteoRemoteDataSource
import com.example.weathernow.data.remote.dto.OpenMeteoForecastDto
import com.example.weathernow.data.remote.dto.OpenMeteoGeocodingDto
import com.example.weathernow.data.remote.dto.OpenMeteoLocationDto
import com.example.weathernow.data.repository.LocationRepositoryImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationRepositoryTest {

    private val mockRemoteDataSource = object : OpenMeteoRemoteDataSource {
        override suspend fun getForecast(
            latitude: Double,
            longitude: Double,
            timezone: String,
            forecastDays: Int
        ): OpenMeteoForecastDto = throw UnsupportedOperationException()

        override suspend fun searchLocations(
            name: String,
            count: Int,
            language: String
        ): OpenMeteoGeocodingDto {
            return if (name.contains("London", ignoreCase = true)) {
                OpenMeteoGeocodingDto(
                    results = listOf(
                        OpenMeteoLocationDto(
                            id = 2643743,
                            name = "London",
                            latitude = 51.50853,
                            longitude = -0.12574,
                            country = "United Kingdom",
                            admin1 = "England"
                        )
                    )
                )
            } else {
                OpenMeteoGeocodingDto(results = emptyList())
            }
        }
    }

    private val repository = LocationRepositoryImpl(mockRemoteDataSource)

    @Test
    fun catalog_searchHanoi_returnsHanoiCapital() {
        val resultsDiacritics = VietnamLocationsCatalog.search("Hà Nội")
        assertTrue(resultsDiacritics.isNotEmpty())
        assertEquals("Hà Nội", resultsDiacritics.first().name)
        assertEquals("Thủ đô Hà Nội", resultsDiacritics.first().adminArea)

        val resultsNoDiacritics = VietnamLocationsCatalog.search("ha noi")
        assertTrue(resultsNoDiacritics.isNotEmpty())
        assertEquals("Hà Nội", resultsNoDiacritics.first().name)

        val resultsAlias = VietnamLocationsCatalog.search("hn")
        assertTrue(resultsAlias.isNotEmpty())
        assertEquals("Hà Nội", resultsAlias.first().name)
    }

    @Test
    fun catalog_searchThaiBinh_returnsThaiBinhUnderHungYen() {
        val results = VietnamLocationsCatalog.search("Thái Bình")
        assertTrue(results.isNotEmpty())
        val match = results.first { it.name == "Thái Bình" }
        assertEquals("Tỉnh Hưng Yên", match.adminArea)
        assertEquals(20.4500, match.latitude, 0.01)
    }

    @Test
    fun catalog_searchHungYen_returnsHungYenProvince() {
        val results = VietnamLocationsCatalog.search("Hưng Yên")
        assertTrue(results.isNotEmpty())
        val match = results.first { it.name == "Hưng Yên" }
        assertEquals("Tỉnh Hưng Yên", match.adminArea)
    }

    @Test
    fun catalog_searchSaigon_returnsHoChiMinhCity() {
        val results = VietnamLocationsCatalog.search("Sài Gòn")
        assertTrue(results.isNotEmpty())
        assertEquals("TP. Hồ Chí Minh", results.first().name)
    }

    @Test
    fun repository_hybridSearch_returnsLocalAndRemoteResults() = runTest {
        val vnRes = repository.searchLocations("Hà Nội")
        assertTrue(vnRes is Resource.Success)
        val vnList = (vnRes as Resource.Success).data
        assertEquals("Hà Nội", vnList.first().name)

        val remoteRes = repository.searchLocations("London")
        assertTrue(remoteRes is Resource.Success)
        val remoteList = (remoteRes as Resource.Success).data
        assertEquals("London", remoteList.first().name)
    }

    // --- Device location tests ---

    @Test
    fun deviceLocation_providerSuccess_returnsNonHanoiCoordinates() = runTest {
        val fakeProvider = object : DeviceLocationDataSource {
            override suspend fun getCurrentCoordinates(): Resource<DeviceCoordinates> {
                return Resource.Success(DeviceCoordinates(latitude = 10.8231, longitude = 106.6297))
            }
        }
        val repo = LocationRepositoryImpl(
            remoteDataSource = mockRemoteDataSource,
            deviceLocationDataSource = fakeProvider
        )

        val result = repo.getCurrentDeviceLocation()
        assertTrue("Expected Resource.Success, got $result", result is Resource.Success)
        val location = (result as Resource.Success).data
        assertEquals(10.8231, location.latitude, 0.0001)
        assertEquals(106.6297, location.longitude, 0.0001)
        assertEquals("Current location", location.name)
        assertEquals("device_10.8231_106.6297", location.id)
        // Must not be Hanoi
        assertNotEquals(21.0285, location.latitude, 0.1)
        assertNotEquals(105.8542, location.longitude, 0.1)
    }

    @Test
    fun deviceLocation_providerError_returnsResourceError() = runTest {
        val fakeProvider = object : DeviceLocationDataSource {
            override suspend fun getCurrentCoordinates(): Resource<DeviceCoordinates> {
                return Resource.Error("Unable to obtain device location")
            }
        }
        val repo = LocationRepositoryImpl(
            remoteDataSource = mockRemoteDataSource,
            deviceLocationDataSource = fakeProvider
        )

        val result = repo.getCurrentDeviceLocation()
        assertTrue("Expected Resource.Error, got $result", result is Resource.Error)
    }

    @Test
    fun deviceLocation_missingProvider_returnsResourceError() = runTest {
        val repo = LocationRepositoryImpl(
            remoteDataSource = mockRemoteDataSource,
            deviceLocationDataSource = null
        )

        val result = repo.getCurrentDeviceLocation()
        assertTrue("Expected Resource.Error, got $result", result is Resource.Error)
    }
}

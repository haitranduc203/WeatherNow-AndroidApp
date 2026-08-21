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
import com.example.weathernow.domain.model.WeatherLocation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
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

    @Test
    fun saveRecentSearch_preservesStableDeviceId() = runTest {
        var insertedEntity: com.example.weathernow.data.local.db.entity.RecentSearchEntity? = null
        var deleteDeviceSearchesCallCount = 0
        val mockRecentDao = object : com.example.weathernow.data.local.db.dao.RecentSearchDao {
            override fun observeRecentSearches(limit: Int) = kotlinx.coroutines.flow.flowOf(emptyList<com.example.weathernow.data.local.db.entity.RecentSearchEntity>())
            override suspend fun insertSearch(entity: com.example.weathernow.data.local.db.entity.RecentSearchEntity): Long {
                insertedEntity = entity
                return 1L
            }
            override suspend fun deleteSearchById(id: String): Int = 1
            override suspend fun deleteDeviceLocationSearches(): Int {
                deleteDeviceSearchesCallCount++
                return 1
            }
            override suspend fun clearRecentSearches(): Int = 0
            override suspend fun getSearchCount(): Int = 1
        }

        val repo = LocationRepositoryImpl(
            remoteDataSource = mockRemoteDataSource,
            recentSearchDao = mockRecentDao
        )

        val deviceLoc = WeatherLocation(
            id = "device_10.8231_106.6297",
            name = "Current location",
            country = null,
            latitude = 10.8231,
            longitude = 106.6297
        )
        repo.saveRecentSearch(deviceLoc)

        assertNotNull("Recent search entity must be inserted", insertedEntity)
        assertEquals("device_10.8231_106.6297", insertedEntity!!.id)
        assertEquals("Cleanup must occur for device location", 1, deleteDeviceSearchesCallCount)

        // Verify ordinary locations do not trigger device-row cleanup
        val normalLoc = WeatherLocation(
            id = "tokyo",
            name = "Tokyo",
            country = "Japan",
            latitude = 35.6762,
            longitude = 139.6503
        )
        repo.saveRecentSearch(normalLoc)
        assertEquals("Cleanup must NOT occur for normal location", 1, deleteDeviceSearchesCallCount)
    }

    @Test
    fun saveRecentSearch_duplicateDeviceLocationCleanup_keepsOnlyLatestDeviceLocation() = runTest {
        val storedEntities = mutableListOf<com.example.weathernow.data.local.db.entity.RecentSearchEntity>()
        val entitiesFlow = kotlinx.coroutines.flow.MutableStateFlow<List<com.example.weathernow.data.local.db.entity.RecentSearchEntity>>(emptyList())

        fun syncFlow() {
            entitiesFlow.value = storedEntities.sortedByDescending { it.searchedAt }
        }

        val fakeRecentDao = object : com.example.weathernow.data.local.db.dao.RecentSearchDao {
            override fun observeRecentSearches(limit: Int) = entitiesFlow
            override suspend fun insertSearch(entity: com.example.weathernow.data.local.db.entity.RecentSearchEntity): Long {
                storedEntities.removeAll { it.id == entity.id }
                storedEntities.add(entity)
                syncFlow()
                return 1L
            }
            override suspend fun deleteSearchById(id: String): Int {
                val removed = storedEntities.removeAll { it.id == id }
                syncFlow()
                return if (removed) 1 else 0
            }
            override suspend fun deleteDeviceLocationSearches(): Int {
                val initial = storedEntities.size
                storedEntities.removeAll {
                    it.id.startsWith("device_") || (it.name.equals("Current location", ignoreCase = true) && it.country == null)
                }
                val removed = initial - storedEntities.size
                syncFlow()
                return removed
            }
            override suspend fun clearRecentSearches(): Int {
                val size = storedEntities.size
                storedEntities.clear()
                syncFlow()
                return size
            }
            override suspend fun getSearchCount(): Int = storedEntities.size
        }

        // Start with legacy current-location row and a normal row (Tokyo)
        fakeRecentDao.insertSearch(
            com.example.weathernow.data.local.db.entity.RecentSearchEntity(
                id = "20.39_106.46",
                name = "Current location",
                country = null,
                adminArea = null,
                latitude = 20.39,
                longitude = 106.46,
                searchedAt = 1000L
            )
        )
        fakeRecentDao.insertSearch(
            com.example.weathernow.data.local.db.entity.RecentSearchEntity(
                id = "tokyo",
                name = "Tokyo",
                country = "Japan",
                adminArea = "Tokyo",
                latitude = 35.6762,
                longitude = 139.6503,
                searchedAt = 2000L
            )
        )

        val repo = LocationRepositoryImpl(
            remoteDataSource = mockRemoteDataSource,
            recentSearchDao = fakeRecentDao
        )

        // Save device location A
        val locationA = WeatherLocation(
            id = "device_20.3904_106.4642",
            name = "Current location",
            country = null,
            latitude = 20.3904,
            longitude = 106.4642
        )
        repo.saveRecentSearch(locationA)

        // Save device location B with slightly different coordinates
        val locationB = WeatherLocation(
            id = "device_20.3910_106.4650",
            name = "Current location",
            country = null,
            latitude = 20.3910,
            longitude = 106.4650
        )
        repo.saveRecentSearch(locationB)

        val recents = repo.observeRecentSearches(10).first()

        // Assert exactly one semantic device location exists
        val deviceEntries = recents.filter {
            it.id?.startsWith("device_") == true || (it.name.equals("Current location", ignoreCase = true) && it.country == null)
        }
        assertEquals("Must contain exactly one semantic device-location entry", 1, deviceEntries.size)

        val activeDevice = deviceEntries.first()
        assertEquals("device_20.3910_106.4650", activeDevice.id)
        assertEquals(20.3910, activeDevice.latitude, 0.0001)
        assertEquals(106.4650, activeDevice.longitude, 0.0001)

        // Assert normal rows remain
        val tokyoEntry = recents.find { it.id == "tokyo" }
        assertNotNull("Normal searches (Tokyo) must remain", tokyoEntry)
        assertEquals("Tokyo", tokyoEntry!!.name)
    }
}

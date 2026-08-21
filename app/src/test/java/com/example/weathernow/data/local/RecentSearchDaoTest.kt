package com.example.weathernow.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.weathernow.data.local.db.WeatherDatabase
import com.example.weathernow.data.local.db.dao.RecentSearchDao
import com.example.weathernow.data.local.db.entity.RecentSearchEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecentSearchDaoTest {

    private lateinit var database: WeatherDatabase
    private lateinit var dao: RecentSearchDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.recentSearchDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndObserveRecentSearches() = runTest {
        val item1 = RecentSearchEntity("vn_hanoi", "Hà Nội", "Việt Nam", "Thủ đô Hà Nội", 21.0285, 105.8542, searchedAt = 1000L)
        val item2 = RecentSearchEntity("tokyo", "Tokyo", "Japan", "Tokyo", 35.6762, 139.6503, searchedAt = 2000L)

        dao.insertSearch(item1)
        dao.insertSearch(item2)

        val list = dao.observeRecentSearches(10).first()
        assertEquals(2, list.size)
        assertEquals("Tokyo", list[0].name) // Most recent first
        assertEquals("Hà Nội", list[1].name)
    }

    @Test
    fun clearRecentSearchesEmptiesTable() = runTest {
        dao.insertSearch(RecentSearchEntity("tokyo", "Tokyo", "Japan", "Tokyo", 35.6762, 139.6503))
        assertEquals(1, dao.getSearchCount())

        dao.clearRecentSearches()
        assertEquals(0, dao.getSearchCount())
    }

    @Test
    fun deleteDeviceLocationSearches_removesOnlySemanticDeviceRows() = runTest {
        val legacyRow = RecentSearchEntity(
            id = "20.39_106.46",
            name = "Current location",
            country = null,
            adminArea = null,
            latitude = 20.39,
            longitude = 106.46,
            searchedAt = 1000L
        )
        val deviceRow = RecentSearchEntity(
            id = "device_20.3904_106.4642",
            name = "Current location",
            country = null,
            adminArea = null,
            latitude = 20.3904,
            longitude = 106.4642,
            searchedAt = 2000L
        )
        val tokyoRow = RecentSearchEntity(
            id = "tokyo",
            name = "Tokyo",
            country = "Japan",
            adminArea = "Tokyo",
            latitude = 35.6762,
            longitude = 139.6503,
            searchedAt = 3000L
        )

        dao.insertSearch(legacyRow)
        dao.insertSearch(deviceRow)
        dao.insertSearch(tokyoRow)
        assertEquals(3, dao.getSearchCount())

        val deletedCount = dao.deleteDeviceLocationSearches()
        assertEquals(2, deletedCount)

        val remaining = dao.observeRecentSearches(10).first()
        assertEquals(1, remaining.size)
        assertEquals("tokyo", remaining[0].id)
        assertEquals("Tokyo", remaining[0].name)
    }
}

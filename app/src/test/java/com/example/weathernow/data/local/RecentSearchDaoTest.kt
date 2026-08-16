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
}

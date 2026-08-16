package com.example.weathernow.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.weathernow.data.local.db.WeatherDatabase
import com.example.weathernow.data.local.db.dao.FavoriteLocationDao
import com.example.weathernow.data.local.db.entity.FavoriteLocationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteLocationDaoTest {

    private lateinit var database: WeatherDatabase
    private lateinit var dao: FavoriteLocationDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.favoriteLocationDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndObserveFavorites() = runTest {
        val hanoi = FavoriteLocationEntity(
            id = "vn_hanoi",
            name = "Hà Nội",
            country = "Việt Nam",
            adminArea = "Thủ đô Hà Nội",
            latitude = 21.0285,
            longitude = 105.8542
        )
        val tokyo = FavoriteLocationEntity(
            id = "tokyo",
            name = "Tokyo",
            country = "Japan",
            adminArea = "Tokyo",
            latitude = 35.6762,
            longitude = 139.6503
        )

        dao.insertFavorite(hanoi)
        dao.insertFavorite(tokyo)

        val list = dao.observeFavorites().first()
        assertEquals(2, list.size)

        assertTrue(dao.isFavoriteSync(21.0285, 105.8542))
        assertFalse(dao.isFavoriteSync(48.8566, 2.3522))
    }

    @Test
    fun deleteFavoriteRemovesCorrectItem() = runTest {
        val hanoi = FavoriteLocationEntity(
            id = "vn_hanoi",
            name = "Hà Nội",
            country = "Việt Nam",
            latitude = 21.0285,
            longitude = 105.8542
        )
        dao.insertFavorite(hanoi)
        assertEquals(1, dao.getFavoriteCount())

        dao.deleteFavoriteById("vn_hanoi")
        assertEquals(0, dao.getFavoriteCount())
        assertFalse(dao.isFavoriteSync(21.0285, 105.8542))
    }
}

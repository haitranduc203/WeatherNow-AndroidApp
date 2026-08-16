package com.example.weathernow.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.weathernow.data.local.db.WeatherDatabase
import com.example.weathernow.data.local.db.dao.CachedWeatherDao
import com.example.weathernow.data.local.db.entity.CachedWeatherEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CachedWeatherDaoTest {

    private lateinit var database: WeatherDatabase
    private lateinit var dao: CachedWeatherDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.cachedWeatherDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetCachedWeather() = runTest {
        val entity = CachedWeatherEntity(
            locationKey = "21.03_105.85",
            latitude = 21.0285,
            longitude = 105.8542,
            cityName = "Hà Nội",
            temperatureCelsius = 28.5,
            feelsLikeCelsius = 31.0,
            conditionName = "CLEAR",
            isDay = true,
            observedAtEpochMillis = 1700000000000L,
            hourlyJson = "[]",
            dailyJson = "[]",
            fetchedAtEpochMillis = 1700000000000L
        )

        dao.insertOrReplace(entity)
        val loaded = dao.getCachedWeather("21.03_105.85")

        assertNotNull(loaded)
        assertEquals("21.03_105.85", loaded!!.locationKey)
        assertEquals(28.5, loaded.temperatureCelsius, 0.01)
        assertEquals("CLEAR", loaded.conditionName)
    }

    @Test
    fun deleteExpiredCacheRemovesOldRecords() = runTest {
        val oldEntity = CachedWeatherEntity(
            locationKey = "old_loc",
            latitude = 10.0,
            longitude = 10.0,
            cityName = "Old",
            temperatureCelsius = 20.0,
            feelsLikeCelsius = 20.0,
            conditionName = "CLOUDY",
            isDay = true,
            observedAtEpochMillis = 1000L,
            hourlyJson = "[]",
            dailyJson = "[]",
            fetchedAtEpochMillis = 1000L
        )
        val newEntity = CachedWeatherEntity(
            locationKey = "new_loc",
            latitude = 20.0,
            longitude = 20.0,
            cityName = "New",
            temperatureCelsius = 25.0,
            feelsLikeCelsius = 25.0,
            conditionName = "CLEAR",
            isDay = true,
            observedAtEpochMillis = 5000L,
            hourlyJson = "[]",
            dailyJson = "[]",
            fetchedAtEpochMillis = 5000L
        )

        dao.insertOrReplace(oldEntity)
        dao.insertOrReplace(newEntity)

        val deleted = dao.deleteExpiredCache(cutoffEpochMillis = 3000L)
        assertEquals(1, deleted)

        assertNull(dao.getCachedWeather("old_loc"))
        assertNotNull(dao.getCachedWeather("new_loc"))
    }
}

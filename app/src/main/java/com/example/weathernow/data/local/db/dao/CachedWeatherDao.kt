package com.example.weathernow.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weathernow.data.local.db.entity.CachedWeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface CachedWeatherDao {

    @Query("SELECT * FROM cached_weather WHERE locationKey = :locationKey LIMIT 1")
    fun observeCachedWeather(locationKey: String): Flow<CachedWeatherEntity?>

    @Query("SELECT * FROM cached_weather WHERE locationKey = :locationKey LIMIT 1")
    suspend fun getCachedWeather(locationKey: String): CachedWeatherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: CachedWeatherEntity): Long

    @Query("DELETE FROM cached_weather WHERE fetchedAtEpochMillis < :cutoffEpochMillis")
    suspend fun deleteExpiredCache(cutoffEpochMillis: Long): Int

    @Query("DELETE FROM cached_weather")
    suspend fun clearAllCache(): Int

    @Query("SELECT COUNT(*) FROM cached_weather")
    suspend fun getCacheCount(): Int
}

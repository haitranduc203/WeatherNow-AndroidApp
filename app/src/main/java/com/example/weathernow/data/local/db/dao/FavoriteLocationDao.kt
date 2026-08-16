package com.example.weathernow.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weathernow.data.local.db.entity.FavoriteLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteLocationDao {

    @Query("SELECT * FROM favorite_locations ORDER BY isPinned DESC, displayOrder ASC, createdAt DESC")
    fun observeFavorites(): Flow<List<FavoriteLocationEntity>>

    @Query("SELECT * FROM favorite_locations WHERE id = :id LIMIT 1")
    fun getFavoriteById(id: String): FavoriteLocationEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_locations WHERE ABS(latitude - :latitude) < 0.05 AND ABS(longitude - :longitude) < 0.05)")
    fun isFavorite(latitude: Double, longitude: Double): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_locations WHERE ABS(latitude - :latitude) < 0.05 AND ABS(longitude - :longitude) < 0.05)")
    fun isFavoriteSync(latitude: Double, longitude: Double): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavorite(entity: FavoriteLocationEntity): Long

    @Query("DELETE FROM favorite_locations WHERE id = :id")
    fun deleteFavoriteById(id: String): Int

    @Query("DELETE FROM favorite_locations WHERE ABS(latitude - :latitude) < 0.05 AND ABS(longitude - :longitude) < 0.05")
    fun deleteFavoriteByCoords(latitude: Double, longitude: Double): Int

    @Query("SELECT COUNT(*) FROM favorite_locations")
    fun getFavoriteCount(): Int
}

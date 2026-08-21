package com.example.weathernow.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weathernow.data.local.db.entity.RecentSearchEntity
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface RecentSearchDao {

    @Query("SELECT * FROM recent_searches ORDER BY searchedAt DESC LIMIT :limit")
    fun observeRecentSearches(limit: Int = 10): Flow<List<RecentSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(entity: RecentSearchEntity): Long

    @Query("DELETE FROM recent_searches WHERE id = :id")
    suspend fun deleteSearchById(id: String): Int

    @Query("DELETE FROM recent_searches")
    suspend fun clearRecentSearches(): Int

    @Query("SELECT COUNT(*) FROM recent_searches")
    suspend fun getSearchCount(): Int
}

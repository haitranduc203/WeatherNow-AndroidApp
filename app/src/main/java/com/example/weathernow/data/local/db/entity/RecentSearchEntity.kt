package com.example.weathernow.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores recent location search queries / selections.
 */
@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val id: String, // e.g. "lat_lon" or unique search key
    val name: String,
    val country: String? = null,
    val adminArea: String? = null,
    val latitude: Double,
    val longitude: Double,
    val searchedAt: Long = System.currentTimeMillis()
)

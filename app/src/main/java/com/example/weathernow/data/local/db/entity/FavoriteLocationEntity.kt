package com.example.weathernow.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores bookmarked/favorite locations saved by the user.
 */
@Entity(tableName = "favorite_locations")
data class FavoriteLocationEntity(
    @PrimaryKey val id: String, // e.g. "21.03_105.85" or UUID
    val name: String,
    val country: String? = null,
    val adminArea: String? = null,
    val latitude: Double,
    val longitude: Double,
    val timezone: String? = null,
    val isPinned: Boolean = false,
    val displayOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

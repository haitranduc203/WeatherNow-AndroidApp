package com.example.weathernow.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weathernow.data.local.db.dao.CachedWeatherDao
import com.example.weathernow.data.local.db.dao.FavoriteLocationDao
import com.example.weathernow.data.local.db.dao.RecentSearchDao
import com.example.weathernow.data.local.db.entity.CachedWeatherEntity
import com.example.weathernow.data.local.db.entity.FavoriteLocationEntity
import com.example.weathernow.data.local.db.entity.RecentSearchEntity

@Database(
    entities = [
        CachedWeatherEntity::class,
        FavoriteLocationEntity::class,
        RecentSearchEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun cachedWeatherDao(): CachedWeatherDao
    abstract fun favoriteLocationDao(): FavoriteLocationDao
    abstract fun recentSearchDao(): RecentSearchDao

    companion object {
        private const val DATABASE_NAME = "weathernow.db"

        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getInstance(context: Context): WeatherDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }

        fun getInstanceOrNull(): WeatherDatabase? = INSTANCE

        fun setInstanceForTesting(db: WeatherDatabase?) {
            INSTANCE = db
        }
    }
}

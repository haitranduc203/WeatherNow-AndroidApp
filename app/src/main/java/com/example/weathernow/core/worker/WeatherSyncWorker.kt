package com.example.weathernow.core.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weathernow.WeatherNowApp
import com.example.weathernow.core.common.Resource
import com.example.weathernow.core.notification.WeatherNotificationManager
import com.example.weathernow.presentation.util.WeatherUnitsFormatter
import kotlinx.coroutines.flow.first
import java.io.IOException

class WeatherSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): androidx.work.ListenableWorker.Result {
        val app = applicationContext as? WeatherNowApp
            ?: return androidx.work.ListenableWorker.Result.failure()

        val appContainer = app.appContainer
        val userPrefsRepo = appContainer.userPreferencesRepository
        val weatherRepo = appContainer.weatherRepository

        val preferences = userPrefsRepo.userPreferences.first()
        if (!preferences.backgroundRefreshEnabled) {
            return androidx.work.ListenableWorker.Result.success()
        }

        return try {
            val favorites = weatherRepo.observeFavoriteLocations().first()
            val targets = if (favorites.isNotEmpty()) {
                favorites.map { fav ->
                    Triple(fav.latitude, fav.longitude, fav.name)
                }
            } else {
                listOf(Triple(21.0285, 105.8542, "Hà Nội"))
            }

            var primaryLocationName = targets.first().third
            var primaryTempFormatted = "N/A"
            var primaryCondition = "Bình thường"
            var primaryHumidity = "65%"

            var syncSuccessCount = 0
            for ((lat, lon, name) in targets) {
                val refreshResult = weatherRepo.refreshWeather(latitude = lat, longitude = lon)
                if (refreshResult is Resource.Success) {
                    syncSuccessCount++
                    if (name == targets.first().third) {
                        val currentResult = weatherRepo.observeCurrentWeather(lat, lon).first { it !is Resource.Loading }
                        if (currentResult is Resource.Success) {
                            val current = currentResult.data
                            primaryLocationName = name
                            primaryTempFormatted = WeatherUnitsFormatter.formatTemperature(
                                current.temperatureCelsius,
                                preferences.temperatureUnit
                            )
                            primaryCondition = current.condition.localizedName(preferences.language)
                            primaryHumidity = "${current.humidityPercent ?: 65}%"
                        }
                    }
                }
            }

            if (syncSuccessCount > 0 && preferences.dailyNotificationEnabled) {
                WeatherNotificationManager.showDailySummaryNotification(
                    context = applicationContext,
                    locationName = primaryLocationName,
                    temperature = primaryTempFormatted,
                    condition = primaryCondition,
                    humidity = primaryHumidity
                )
            }

            if (syncSuccessCount > 0) {
                androidx.work.ListenableWorker.Result.success()
            } else {
                androidx.work.ListenableWorker.Result.retry()
            }
        } catch (e: IOException) {
            androidx.work.ListenableWorker.Result.retry()
        } catch (e: Exception) {
            androidx.work.ListenableWorker.Result.failure()
        }
    }
}

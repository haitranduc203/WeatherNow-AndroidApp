package com.example.weathernow.core.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.weathernow.core.common.Constants
import java.util.concurrent.TimeUnit

object WeatherWorkScheduler {

    private fun buildConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
    }

    fun schedulePeriodicSync(context: Context, intervalHours: Long = 3) {
        val constraints = buildConstraints()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<WeatherSyncWorker>(
            intervalHours,
            TimeUnit.HOURS,
            15,
            TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                15,
                TimeUnit.MINUTES
            )
            .addTag("weather_sync_periodic")
            .build()

        try {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                Constants.WEATHER_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWorkRequest
            )
        } catch (_: Exception) {}
    }

    fun cancelPeriodicSync(context: Context) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(Constants.WEATHER_WORK_NAME)
        } catch (_: Exception) {}
    }

    fun triggerOneTimeSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<WeatherSyncWorker>()
            .setConstraints(constraints)
            .addTag("weather_sync_onetime")
            .build()

        try {
            WorkManager.getInstance(context).enqueueUniqueWork(
                "${Constants.WEATHER_WORK_NAME}_onetime",
                ExistingWorkPolicy.REPLACE,
                oneTimeWorkRequest
            )
        } catch (_: Exception) {}
    }
}

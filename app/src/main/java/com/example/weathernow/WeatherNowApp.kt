package com.example.weathernow

import android.app.Application
import com.example.weathernow.core.di.AppContainer
import com.example.weathernow.core.di.DefaultAppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherNowApp : Application() {

    lateinit var container: AppContainer
    val appContainer: AppContainer get() = container

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = DefaultAppContainer(this)

        // Initialize Notification Channel
        com.example.weathernow.core.notification.WeatherNotificationManager.createNotificationChannel(this)

        // Bind persistent DataStore to in-memory preferences flow and WorkManager scheduler
        CoroutineScope(Dispatchers.Main).launch {
            container.userPreferencesRepository.userPreferences.collect { prefs ->
                com.example.weathernow.presentation.settings.UserPreferencesRepository.syncFromDataStore(prefs)
                if (prefs.backgroundRefreshEnabled) {
                    com.example.weathernow.core.worker.WeatherWorkScheduler.schedulePeriodicSync(this@WeatherNowApp)
                } else {
                    com.example.weathernow.core.worker.WeatherWorkScheduler.cancelPeriodicSync(this@WeatherNowApp)
                }
            }
        }
    }

    companion object {
        var instance: WeatherNowApp? = null
            private set
    }
}

package com.example.weathernow

import android.app.Application
import com.example.weathernow.core.di.AppContainer
import com.example.weathernow.core.di.DefaultAppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherNowApp : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = DefaultAppContainer(this)

        // Bind persistent DataStore to in-memory preferences flow
        CoroutineScope(Dispatchers.Main).launch {
            container.userPreferencesRepository.userPreferences.collect { prefs ->
                com.example.weathernow.presentation.settings.UserPreferencesRepository.syncFromDataStore(prefs)
            }
        }
    }

    companion object {
        var instance: WeatherNowApp? = null
            private set
    }
}

package com.example.weathernow

import android.app.Application
import com.example.weathernow.core.di.AppContainer
import com.example.weathernow.core.di.DefaultAppContainer

class WeatherNowApp : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = DefaultAppContainer(this)
    }

    companion object {
        lateinit var instance: WeatherNowApp
            private set
    }
}

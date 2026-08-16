package com.example.weathernow.core.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.weathernow.core.notification.WeatherNotificationManager

class WeatherTestNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.example.weathernow.TEST_NOTIFICATION" -> {
                val locationName = intent.getStringExtra("location") ?: "Hà Nội, Việt Nam"
                val temp = intent.getStringExtra("temp") ?: "34°C"
                val condition = intent.getStringExtra("condition") ?: "Có mây rải rác"
                val humidity = intent.getStringExtra("humidity") ?: "66%"
                WeatherNotificationManager.showDailySummaryNotification(
                    context = context,
                    locationName = locationName,
                    temperature = temp,
                    condition = condition,
                    humidity = humidity
                )
            }
            "com.example.weathernow.TEST_SYNC" -> {
                WeatherWorkScheduler.triggerOneTimeSync(context)
            }
        }
    }
}

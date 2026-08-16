package com.example.weathernow.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.weathernow.MainActivity
import com.example.weathernow.R
import com.example.weathernow.core.common.Constants

object WeatherNotificationManager {

    private const val NOTIFICATION_ID_DAILY_SUMMARY = 1001
    private const val NOTIFICATION_ID_SEVERE_ALERT = 1002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Weather Alerts & Summaries"
            val descriptionText = "Periodic daily forecast summaries and severe weather alerts"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                Constants.WEATHER_NOTIFICATION_CHANNEL_ID,
                name,
                importance
            ).apply {
                description = descriptionText
                enableVibration(true)
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showDailySummaryNotification(
        context: Context,
        locationName: String,
        temperature: String,
        condition: String,
        humidity: String? = null
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (!humidity.isNullOrBlank()) {
            "$condition • $temperature • Độ ẩm $humidity"
        } else {
            "$condition • $temperature"
        }

        val builder = NotificationCompat.Builder(context, Constants.WEATHER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Bản tin thời tiết: $locationName")
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Thời tiết hiện tại tại $locationName: $temperature, $condition. Chúc bạn một ngày tốt lành!")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DAILY_SUMMARY, builder.build())
        } catch (_: SecurityException) {
            // Notification permission might not be granted on Android 13+
        }
    }

    fun showSevereWeatherAlert(
        context: Context,
        locationName: String,
        alertTitle: String,
        alertDescription: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, Constants.WEATHER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ Cảnh báo thời tiết: $locationName")
            .setContentText(alertTitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alertDescription))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SEVERE_ALERT, builder.build())
        } catch (_: SecurityException) {
            // Notification permission might not be granted
        }
    }
}

package com.example.prayertime.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    const val PRAYER_CHANNEL_ID = "prayer_channel"
    const val ALARM_CHANNEL_ID = "alarm_channel"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val prayerChannel = NotificationChannel(
                PRAYER_CHANNEL_ID,
                "مواقيت الصلاة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات مواقيت الصلاة"
            }

            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "الأذان والتنبيه",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات الأذان والتنبيه"
            }

            manager.createNotificationChannels(listOf(prayerChannel, alarmChannel))
        }
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}

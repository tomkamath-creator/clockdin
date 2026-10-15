// Location: app/src/main/java/com/vk/clockdin/ClockdinApp.kt
package com.vk.clockdin

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ClockdinApp : Application() {

    companion object {
        const val REMINDER_CHANNEL_ID = "clockdin_daily_reminder"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val name = getString(R.string.reminder_channel_name)
        val descriptionText = getString(R.string.reminder_channel_desc)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(REMINDER_CHANNEL_ID, name, importance).apply {
            description = descriptionText
            enableLights(true)
            enableVibration(true)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }
}

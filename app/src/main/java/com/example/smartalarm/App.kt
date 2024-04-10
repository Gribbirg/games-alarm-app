package com.example.smartalarm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.google.android.material.color.DynamicColors

/**
 * Application
 *
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    private fun createNotificationChannel() {
        val id = "smartalarm"
        val name = "channelName"
        val descriptionText = "channelDesc"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel(id, name, importance)
        mChannel.description = descriptionText
        val notificationManager = getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }
}
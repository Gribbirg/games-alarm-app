package com.example.smartalarm.data.repositories

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.services.AlarmReceiver

class AlarmCreateRepository(
    private val context: Context
) {

    private val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

    fun create(alarm: AlarmData) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm id", alarm.id)
            putExtra("alarm time", alarm.milisTime)
            putExtra("alarm name", alarm.name)
            putExtra("alarm vibration", alarm.isVibration)
            putExtra("alarm rising volume", alarm.isRisingVolume)
            putExtra("alarm ringtone path", alarm.ringtonePath)
        }
        Log.i("alarm", "Alarm on create!")
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarm.milisTime,
            pendingIntent
        )
    }

    fun cancel(alarm: AlarmData) {
        Log.i("alarm", "Alarm on delete!")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.cancel(
                PendingIntent.getBroadcast(
                    context,
                    alarm.id.toInt(),
                    Intent(context, AlarmReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            )
        } else {
            alarmManager.cancel(
                PendingIntent.getBroadcast(
                    context,
                    alarm.id.toInt(),
                    Intent(context, AlarmReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        }
    }

    fun update(alarm: AlarmData) {
        Log.i("alarm", "Alarm on update!")
        cancel(alarm)
        create(alarm)
    }
}
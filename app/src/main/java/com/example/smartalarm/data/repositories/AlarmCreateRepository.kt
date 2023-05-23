package com.example.smartalarm.data.repositories

import android.app.AlarmManager
import android.app.PendingIntent

import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.smartalarm.data.receivers.AlarmReceiver
import com.example.smartalarm.data.data.AlarmData
import java.time.ZoneId

class AlarmCreateRepository(
    private val context: Context
)  {

    private val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

    fun create(alarm: AlarmData) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm id", alarm.alarmSimpleData.id)
            putExtra("alarm vibration", alarm.alarmSimpleData.isVibration)
            putExtra("alarm rising volume", alarm.alarmSimpleData.isRisingVolume)
        }
        Log.i("alarm", "Alarm on create!")
        val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.alarmSimpleData.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarm.localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000,
            pendingIntent
        )
    }
    fun cancel(alarm: AlarmData) {
        Log.i("alarm", "Alarm on delete!")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.cancel(
                PendingIntent.getBroadcast(
                    context,
                    alarm.alarmSimpleData.id.toInt(),
                    Intent(context, AlarmReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            )
        } else {
            alarmManager.cancel(
                PendingIntent.getBroadcast(
                    context,
                    alarm.alarmSimpleData.id.toInt(),
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
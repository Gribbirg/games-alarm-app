package com.example.smartalarm.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent

import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.data.db.AlarmSimpleData
import java.time.ZoneId

class AlarmCreateRepository(
    private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager


    override fun schedule(item: AlarmData) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm id", item.alarmSimpleData.id)
        }
        Log.i("grib", (item.localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000).toString())
        val pendingIntent = PendingIntent.getBroadcast(
                context,
                item.alarmSimpleData.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            item.localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000,
            pendingIntent
        )
    }

    override fun cancel(item: AlarmData) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                item.alarmSimpleData.id.toInt(),
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        )
    }
}
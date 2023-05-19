package com.example.smartalarm.data.repositories

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.data.receivers.AlarmReceiver
import kotlinx.coroutines.CoroutineScope

class AlarmCreateRepository(
    private val context: Context
): AppCompatActivity() {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(item: AlarmSimpleData) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA MESSAGE", item.name)
        }
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            ((item.timeHour * 60 * 60 + item.timeMinute * 60) * 1000).toLong(),
            PendingIntent.getBroadcast(
                context,
                item.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        )
    }

    fun cancel(item: AlarmData) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                item.hashCode(),
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        )
    }
}
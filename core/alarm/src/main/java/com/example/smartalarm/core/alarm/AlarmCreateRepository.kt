package com.example.smartalarm.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.smartalarm.core.data.model.AlarmData

/**
 * On and off alarms
 *
 * @property context
 */
class AlarmCreateRepository(
    private val context: Context
) {

    private val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

    /**
     * Turn on Alarm
     *
     * @param alarm
     */
    fun create(alarm: AlarmData) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmIntentKeys.ALARM_ID, alarm.id)
            putExtra(AlarmIntentKeys.ALARM_TIME, alarm.milisTime)
            putExtra(AlarmIntentKeys.ALARM_NAME, alarm.name)
            putExtra(AlarmIntentKeys.ALARM_VIBRATION, alarm.isVibration)
            putExtra(AlarmIntentKeys.ALARM_RISING_VOLUME, alarm.isRisingVolume)
            putExtra(AlarmIntentKeys.ALARM_RINGTONE_PATH, alarm.ringtonePath)
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

    /**
     * Turn off alarm
     *
     * @param alarm
     */
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

    /**
     * Update alarm data
     *
     * @param alarm
     */
    fun update(alarm: AlarmData) {
        Log.i("alarm", "Alarm on update!")
        cancel(alarm)
        create(alarm)
    }
}
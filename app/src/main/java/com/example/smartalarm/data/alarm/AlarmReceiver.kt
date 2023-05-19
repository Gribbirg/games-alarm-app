package com.example.smartalarm.data.alarm

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import com.example.smartalarm.R
import com.example.smartalarm.ui.activities.GamesActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("alarm", "Alarm on start!")
        val alarmId = intent?.getLongExtra("alarm id", 0L) ?: return
        val alarmVibration = intent.getBooleanExtra("alarm vibration", false)
        val alarmRisingVolume = intent.getBooleanExtra("alarm rising volume", false)
        Log.i("alarm", alarmId.toString())

        val intentToActivity = Intent(context, GamesActivity::class.java)
        intentToActivity.putExtra("alarm id", alarmId)
        val pendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            intentToActivity,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )


        val powerManager = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Alarm:alarmLock")
        wakeLock.acquire(10*60*1000L /*10 minutes*/)

        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                2000,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )

        val notificationBuilder = NotificationCompat.Builder(context, "smartalarm")
            .setSmallIcon(R.drawable.baseline_alarm_24)
            .setContentTitle("Разбуди меня полностью")
            .setContentText("Нажми, что бы отключить будильник")
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(alarmId.toInt(), notificationBuilder.build())


    }
}
package com.example.smartalarm.services

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.smartalarm.R
import com.example.smartalarm.ui.activities.GamesActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("alarm", "Alarm on start!")
        val alarmId = intent?.getLongExtra("alarm id", 0L) ?: return
        val alarmTime = intent.getLongExtra("alarm time", 0L)
        val alarmRisingVolume = intent.getBooleanExtra("alarm rising volume", false)
        val alarmVibration = intent.getBooleanExtra("alarm vibration", false)
        val alarmRingtone = intent.getStringExtra("alarm ringtone path")
        Log.i("alarm", "Alarm id: $alarmId")
        Log.i("alarm", "Rising volume: $alarmRisingVolume")
        Log.i("alarm", "Vibration: $alarmVibration")
        Log.i("alarm", "Ringtone: $alarmRingtone")

        val intentToActivity = Intent(context, GamesActivity::class.java)
        intentToActivity.putExtra("alarm id", alarmId)
        intentToActivity.putExtra("start time", alarmTime)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                context,
                alarmId.toInt(),
                intentToActivity,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getActivity(
                context,
                alarmId.toInt(),
                intentToActivity,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val notificationBuilder = NotificationCompat.Builder(context!!, "smartalarm")
            .setSmallIcon(R.drawable.baseline_alarm_24)
            .setContentTitle("${intent.getStringExtra("alarm name")} звонит!")
            .setContentText("Нажмите, чтобы отключить будильник")
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setOngoing(true)

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(alarmId.toInt(), notificationBuilder.build())
        }

        AlarmMediaPlayer.currentAlarmId = alarmId.toInt()
        AlarmMediaPlayer.playAudio(context, alarmRisingVolume, alarmVibration, alarmRingtone!!)
    }
}
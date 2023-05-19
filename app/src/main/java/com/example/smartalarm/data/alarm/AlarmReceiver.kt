package com.example.smartalarm.data.alarm

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.smartalarm.R
import com.example.smartalarm.ui.activities.CountActivity

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("alarm", "Alarm on start!")
        val alarmId = intent?.getLongExtra("alarm id", 0L) ?: return
        Log.i("alarm", alarmId.toString())

        val intentToActivity = Intent(context, CountActivity::class.java)
        intentToActivity.putExtra("alarm id", alarmId)
        val pendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            intentToActivity,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context!!, "smartalarm")
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
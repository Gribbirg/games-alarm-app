package com.example.smartalarm.data.alarm

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.smartalarm.R
import com.example.smartalarm.ui.activities.CountActivity
import java.io.IOException

class AlarmReceiver: BroadcastReceiver() {

    var mediaPlayer: MediaPlayer? = null

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

        playAudio(context)
    }

    private fun playAudio(context: Context?) {
        val audioUrl = "https://www.bensound.com/bensound-music/bensound-ukulele.mp3"
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)

        try {
            mediaPlayer!!.setDataSource(audioUrl)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun pauseAudio(context: Context?) {
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
        }
    }
}
package com.example.smartalarm.data.alarm

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import com.example.smartalarm.R
import com.example.smartalarm.ui.activities.GamesActivity
import java.io.IOException

class AlarmReceiver: BroadcastReceiver() {

    var mediaPlayer: MediaPlayer? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("alarm", "Alarm on start!")
        val alarmId = intent?.getLongExtra("alarm id", 0L) ?: return
        val alarmRisingVolume = intent.getBooleanExtra("alarm rising volume", false)
        val alarmVibration = intent.getBooleanExtra("alarm vibration", false)
        Log.i("alarm", alarmId.toString())
        Log.i("alarm", alarmRisingVolume.toString())
        Log.i("alarm", alarmVibration.toString())

        val intentToActivity = Intent(context, GamesActivity::class.java)
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
        playAudio(context, alarmRisingVolume, alarmVibration)
    }

    private fun playAudio(context: Context?, isRisingVolume: Boolean, vibrationRequired: Boolean) {
        val audioUrl = "https://vgmsite.com/soundtracks/pixel-gun-3d-2014-ios-gamerip/mggwsgzyfq/Arena%20Background.mp3"

        if (vibrationRequired)
        {
            val pattern: LongArray = longArrayOf(1000, 1000, 1000, 1000)
            val v = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            v.vibrate(pattern, 0)
        }

        mediaPlayer = MediaPlayer()
        if (isRisingVolume)
            mediaPlayer!!.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            )
        else
            mediaPlayer!!.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

        mediaPlayer!!.isLooping = true

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
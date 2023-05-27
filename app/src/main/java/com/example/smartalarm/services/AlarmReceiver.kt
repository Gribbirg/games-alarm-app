package com.example.smartalarm.services

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Vibrator
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.smartalarm.R
import com.example.smartalarm.ui.activities.GamesActivity
import java.io.IOException

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        var mediaPlayer: MediaPlayer? = null

        fun stopAudio(context: Context?) {
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                mediaPlayer!!.release()
                mediaPlayer = null
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("alarm", "Alarm on start!")
        val alarmId = intent?.getLongExtra("alarm id", 0L) ?: return
        val alarmRisingVolume = intent.getBooleanExtra("alarm rising volume", false)
        val alarmVibration = intent.getBooleanExtra("alarm vibration", false)
        Log.i("alarm", "Alarm id: $alarmId")
        Log.i("alarm", "Rising volume: $alarmRisingVolume")
        Log.i("alarm", "Vibration: $alarmVibration")

        val intentToActivity = Intent(context, GamesActivity::class.java)
        intentToActivity.putExtra("alarm id", alarmId)
        intentToActivity.putExtra("start time", System.currentTimeMillis())
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
            .setContentText("Нажмите, что бы отключить будильник")
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
        playAudio(context, alarmRisingVolume, alarmVibration)
    }

    private fun playAudio(context: Context?, isRisingVolume: Boolean, vibrationRequired: Boolean) {
        val audioUrl =
            "https://vgmsite.com/soundtracks/pixel-gun-3d-2014-ios-gamerip/mggwsgzyfq/Arena%20Background.mp3"

        AlarmVibrator.setVibrator(context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)

        if (vibrationRequired) {
            val pattern: LongArray = longArrayOf(1000, 1000, 1000, 1000)
            AlarmVibrator.start(pattern, 0)
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
}
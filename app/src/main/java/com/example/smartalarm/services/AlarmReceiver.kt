package com.example.smartalarm.services

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
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
        lateinit var track: Ringtone

        fun stopAudio() {
            //if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            //    Log.i("music", "music off!")
            //    mediaPlayer!!.release()
            //    mediaPlayer = null
            if (track.isPlaying) {
                Log.i("music", "music off!")
                track.stop()
            }
        }
    }

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
        playAudio(context, alarmRisingVolume, alarmVibration, alarmRingtone!!)
    }

    private fun playAudio(context: Context?, isRisingVolume: Boolean, vibrationRequired: Boolean,
                          ringtonePath: String) {

        AlarmVibrator.setVibrator(context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)

        if (vibrationRequired) {
            val pattern: LongArray = longArrayOf(1000, 1000, 1000, 1000)
            AlarmVibrator.start(pattern, 0)
        }

//        mediaPlayer = MediaPlayer()
//        if (isRisingVolume)
//            mediaPlayer!!.setAudioAttributes(
//                AudioAttributes.Builder()
//                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                    .setUsage(AudioAttributes.USAGE_ALARM)
//                    .build()
//            )
//        else
//            mediaPlayer!!.setAudioAttributes(
//                AudioAttributes.Builder()
//                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                    .setUsage(AudioAttributes.USAGE_MEDIA)
//                    .build()
//            )

        val ringtone = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)
        track = RingtoneManager.getRingtone(context, ringtone)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            track.isLooping = true
        }
        track.play()

//        mediaPlayer!!.isLooping = true
        Log.i("music", "music on!")
//        try {
//            mediaPlayer!!.setDataSource(ringtonePath)
//            mediaPlayer!!.prepare()
//            mediaPlayer!!.start()
//
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
    }
}
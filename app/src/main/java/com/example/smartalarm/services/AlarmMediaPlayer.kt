package com.example.smartalarm.services

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Vibrator
import com.example.smartalarm.data.utils.RealPathUtil
import java.io.IOException

class AlarmMediaPlayer {

    companion object {
        var mediaPlayer: MediaPlayer? = null
        var currentAlarmId: Int? = null

        fun stopAudio() {
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                mediaPlayer!!.release()
                mediaPlayer = null
            }
        }

        fun playAudio(
            context: Context?, isRisingVolume: Boolean, vibrationRequired: Boolean,
            ringtonePath: String
        ) {

            val ringtonePathFinal: String? = if (ringtonePath == "null") {
                RealPathUtil.getRealPath(
                    context!!, RingtoneManager.getActualDefaultRingtoneUri(
                        context,
                        RingtoneManager.TYPE_RINGTONE
                    )
                )
            } else {
                ringtonePath
            }

            AlarmVibrator.setVibrator(context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)

            if (vibrationRequired) {
                val pattern: LongArray = longArrayOf(1000, 1000, 1000, 1000)
                AlarmVibrator.start(pattern, 0)
            }

            mediaPlayer = MediaPlayer()
            val audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE)
                    as AudioManager

            if (isRisingVolume || audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) <=
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 4
            )
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
                mediaPlayer!!.setDataSource(ringtonePathFinal)

            } catch (e: IOException) {
                e.printStackTrace()
                mediaPlayer!!.setDataSource(
                    RealPathUtil.getRealPath(
                        context,
                        RingtoneManager.getActualDefaultRingtoneUri(
                            context,
                            RingtoneManager.TYPE_RINGTONE
                        )
                    )
                )
            }

            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
        }
    }
}
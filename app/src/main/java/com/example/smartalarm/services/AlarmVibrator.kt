package com.example.smartalarm.services

import android.os.Vibrator

object AlarmVibrator {
    private lateinit var vibrator: Vibrator

    fun setVibrator(vibrator: Vibrator) {
        this.vibrator = vibrator
    }

    fun start(pattern: LongArray, repeat: Int) {
        vibrator.vibrate(pattern, repeat)
    }

    fun stop() {
        vibrator.cancel()
    }
}
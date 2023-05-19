package com.example.smartalarm.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("grib", "add")
        val message = intent?.getStringExtra("EXTRA MESSAGE") ?: return
        Log.i("grib", message)
    }
}
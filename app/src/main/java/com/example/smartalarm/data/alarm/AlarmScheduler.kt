package com.example.smartalarm.data.alarm

import com.example.smartalarm.data.data.AlarmData

interface AlarmScheduler {
    fun schedule(item: AlarmData)
    fun cancel(item: AlarmData)
}
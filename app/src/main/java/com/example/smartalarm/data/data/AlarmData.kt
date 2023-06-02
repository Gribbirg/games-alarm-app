package com.example.smartalarm.data.data

import android.util.Log
import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.data.repositories.dateListFromString
import com.example.smartalarm.data.repositories.getNearestDate
import java.time.LocalDateTime
import java.time.ZoneId

data class AlarmData(
    var id: Long = 0L,
    var name: String = "Будильник",
    var timeHour: Int = 0,
    var timeMinute: Int = 0,
    var dayOfWeek: Int = 0,
    var activateDate: String? = null,
    var recordScore: Int? = null,
    var recordSeconds: String? = null,
    var isVibration: Boolean = false,
    var isRisingVolume: Boolean = false,
    var isOn: Boolean = true,
    var ringtonePath: String = "",
    var gamesList: ArrayList<Int> = arrayListOf()
) {
    var milisTime: Long = 0L

    constructor(alarmSimpleData: AlarmSimpleData, gamesList: ArrayList<Int> = arrayListOf()) :
            this(
                alarmSimpleData.id,
                alarmSimpleData.name,
                alarmSimpleData.timeHour,
                alarmSimpleData.timeMinute,
                alarmSimpleData.dayOfWeek,
                alarmSimpleData.activateDate,
                alarmSimpleData.recordScore,
                alarmSimpleData.recordSeconds,
                alarmSimpleData.isVibration,
                alarmSimpleData.isRisingVolume,
                alarmSimpleData.isOn,
                alarmSimpleData.ringtonePath,
                gamesList
            )

    init {
        val date =
            if (activateDate == null)
                getNearestDate(
                    dayOfWeek,
                    timeMinute,
                    timeHour
                )
            else dateListFromString(activateDate!!)

        val localDateTime = LocalDateTime.of(
            date[0],
            date[1],
            date[2],
            timeHour,
            timeMinute
        )
        milisTime = localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000
        Log.i("grib", "milis $milisTime")
        Log.i("grib", "system ${System.currentTimeMillis()}")
    }
}

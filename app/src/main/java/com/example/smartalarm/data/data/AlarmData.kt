package com.example.smartalarm.data.data

import android.util.Log
import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.data.repositories.dateListFromString
import com.example.smartalarm.data.repositories.getNearestDate
import java.time.LocalDateTime
import java.time.ZoneId

data class AlarmData(
    var alarmSimpleData: AlarmSimpleData,
    var gamesList: ArrayList<Int> = arrayListOf()
) {
//    var localDateTime: LocalDateTime
    var milisTime: Long

    init {
        val date =
            if (alarmSimpleData.activateDate == null)
                getNearestDate(
                    alarmSimpleData.dayOfWeek,
                    alarmSimpleData.timeMinute,
                    alarmSimpleData.timeHour
                )
            else dateListFromString(alarmSimpleData.activateDate!!)

        val localDateTime = LocalDateTime.of(
            date[0],
            date[1],
            date[2],
            alarmSimpleData.timeHour,
            alarmSimpleData.timeMinute
        )
        milisTime = localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000
        Log.i("grib", "milis $milisTime")
        Log.i("grib", "system ${System.currentTimeMillis()}")
    }
}

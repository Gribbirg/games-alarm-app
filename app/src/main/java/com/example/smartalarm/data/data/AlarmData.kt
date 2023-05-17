package com.example.smartalarm.data.data

import com.example.smartalarm.data.db.AlarmSimpleData

data class AlarmData(
    var alarmSimpleData: AlarmSimpleData,
    var gamesList: ArrayList<Int>
)

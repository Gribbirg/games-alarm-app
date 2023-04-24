package com.example.smartalarm.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_table")
data class AlarmSimpleData(
    @PrimaryKey(autoGenerate = true) val id : Long,
    var timeHour : Int,
    var timeMinute : Int,
    @ColumnInfo(name = "day_of_week") var dayOfWeek : Int,
    var recordMinutes : Int,
    var recordSeconds : Int,
    var name : String,
    var isOn : Boolean
)
package com.example.smartalarm.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_table")
data class AlarmSimpleData(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id : Long = 0L,

    @ColumnInfo(name = "time_hour")
    var timeHour : Int,

    @ColumnInfo(name = "time_minute")
    var timeMinute : Int,

    @ColumnInfo(name = "day_of_week")
    var dayOfWeek : Int,

    @ColumnInfo(name = "record_minutes")
    var recordMinutes : Int,

    @ColumnInfo(name = "record_seconds")
    var recordSeconds : Int,

    @ColumnInfo(name = "name")
    var name : String,

    @ColumnInfo(name = "is_on")
    var isOn : Boolean
) {}
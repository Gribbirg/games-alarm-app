package com.example.smartalarm.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_table")
data class AlarmSimpleData(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0L,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "time_hour")
    var timeHour: Int,

    @ColumnInfo(name = "time_minute")
    var timeMinute: Int,

    @ColumnInfo(name = "day_of_week")
    var dayOfWeek: Int,

    @ColumnInfo(name = "activate_date")
    var activateDate: String?,

    @ColumnInfo(name = "record_score")
    var recordScore: Int?,

    @ColumnInfo(name = "record_time")
    var recordSeconds: String?,

    @ColumnInfo(name = "vibration")
    var isVibration: Boolean,

    @ColumnInfo(name = "rising_volume")
    var isRisingVolume: Boolean,

    @ColumnInfo(name = "is_on")
    var isOn: Boolean = true
)
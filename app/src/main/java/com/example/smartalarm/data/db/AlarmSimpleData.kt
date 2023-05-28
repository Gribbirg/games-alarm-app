package com.example.smartalarm.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.smartalarm.data.data.AlarmData

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
    var isOn: Boolean = true,

    @ColumnInfo(name = "ringtone_path")
    var ringtonePath: String
) {

    constructor(strList: ArrayList<String>) : this(
        strList[0].toLong(),
        strList[1],
        strList[2].toInt(),
        strList[3].toInt(),
        strList[4].toInt(),
        if (strList[5] == "null") null else strList[5],
        if (strList[6] == "null") null else strList[6].toInt(),
        if (strList[7] == "null") null else strList[7],
        strList[8].toBoolean(),
        strList[9].toBoolean(),
        strList[10].toBoolean(),
        strList[11]
    )

    constructor(alarmData: AlarmData) : this(
        alarmData.id,
        alarmData.name,
        alarmData.timeHour,
        alarmData.timeMinute,
        alarmData.dayOfWeek,
        alarmData.activateDate,
        alarmData.recordScore,
        alarmData.recordSeconds,
        alarmData.isVibration,
        alarmData.isRisingVolume,
        alarmData.isOn,
        alarmData.ringtonePath,
    )

    // len = 12
    fun toStringArray(): ArrayList<String> {
        return arrayListOf(
            id.toString(),
            name,
            timeHour.toString(),
            timeMinute.toString(),
            dayOfWeek.toString(),
            activateDate.toString(),
            recordScore.toString(),
            recordSeconds.toString(),
            isVibration.toString(),
            isRisingVolume.toString(),
            isOn.toString(),
            ringtonePath
        )
    }
}
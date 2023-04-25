package com.example.smartalarm.data

import androidx.lifecycle.MutableLiveData
import androidx.room.*

@Dao
interface AlarmsDao {

    @Insert
    fun insertNewAlarmData(alarmSimpleData: AlarmSimpleData?)

    @Query("SELECT * FROM alarm_table ORDER BY id DESC")
    fun getAlarms() : List<AlarmSimpleData>?

    @Update
    fun updateAlarm(alarm : AlarmSimpleData?)

    @Delete
    fun deleteAlarm(alarm: AlarmSimpleData?)
}
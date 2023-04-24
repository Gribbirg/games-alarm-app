package com.example.smartalarm.data

import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Index
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AlarmsDao {

    @Insert
    fun insertNewAlarmData(alarmSimpleData: AlarmSimpleData)

    @Query("SELECT * FROM alarm_table WHERE day_of_week = :dayOfWeek")
    fun getAlarms(dayOfWeek: Int) : List<AlarmSimpleData>
}
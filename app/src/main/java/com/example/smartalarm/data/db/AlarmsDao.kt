package com.example.smartalarm.data.db

import androidx.room.*

@Dao
interface AlarmsDao {

    @Insert(entity = AlarmSimpleData::class)
    fun insertNewAlarmData(alarmSimpleData: AlarmSimpleData)

    @Insert(entity = AlarmInfoData::class)
    fun insertNewAlarmInfoData(alarmInfoData: AlarmInfoData)

    @Insert(entity = AlarmGamesData::class)
    fun insertNewAlarmGamesData(alarmGamesData: AlarmGamesData)

    @Query("SELECT * FROM alarm_table ORDER BY time_hour, time_minute ASC")
    fun getAlarms(): List<AlarmSimpleData>

    @Query("SELECT * FROM alarm_table WHERE day_of_week = :dayOfWeek ORDER BY time_hour, time_minute ASC")
    fun getAlarmsByDay(dayOfWeek: Int): List<AlarmSimpleData>

    @Query("SELECT * FROM alarm_table WHERE id = :id")
    fun getAlarmById(id: Long): AlarmSimpleData

    @Update(entity = AlarmSimpleData::class)
    fun updateAlarm(alarm: AlarmSimpleData)

    @Delete(entity = AlarmSimpleData::class)
    fun deleteAlarm(alarm: AlarmSimpleData)
}
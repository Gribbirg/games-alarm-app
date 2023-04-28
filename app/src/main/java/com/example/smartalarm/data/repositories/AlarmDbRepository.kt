package com.example.smartalarm.data.repositories

import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.data.db.AlarmsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlarmDbRepository(private val alarmsDao: AlarmsDao) {
    suspend fun getAlarmsFromDbByDayOfWeek(dayOfWeek: Int): List<AlarmSimpleData> =
        withContext(Dispatchers.IO) {
            return@withContext alarmsDao.getAlarmsByDay(dayOfWeek)
        }

    suspend fun getAlarmFromDb(id: Long): AlarmSimpleData =
        withContext(Dispatchers.IO) {
            return@withContext alarmsDao.getAlarmById(id)
        }

    suspend fun insertAlarmToDb(alarm: AlarmSimpleData) =
        withContext(Dispatchers.IO) {
            alarmsDao.insertNewAlarmData(alarm)
        }

    suspend fun updateAlarmInDb(alarm: AlarmSimpleData) =
        withContext(Dispatchers.IO) {
            alarmsDao.updateAlarm(alarm)
        }

    suspend fun deleteAlarmFromDb(alarm: AlarmSimpleData) =
        withContext(Dispatchers.IO) {
            alarmsDao.deleteAlarm(alarm)
        }
}
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
}
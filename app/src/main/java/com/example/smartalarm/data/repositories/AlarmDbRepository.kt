package com.example.smartalarm.data.repositories

import android.util.Log
import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.data.db.AlarmsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlarmDbRepository(private val alarmsDao: AlarmsDao) {
    suspend fun getAlarmsFromDbByDayOfWeek(
        dayOfWeek: Int,
        currentDate: String
    ): ArrayList<AlarmSimpleData> =
        withContext(Dispatchers.IO) {
            val listDB = ArrayList(alarmsDao.getAlarmsByDay(dayOfWeek))
            val listAns = ArrayList<AlarmSimpleData>()

            for (alarm in listDB) {
                if (alarm.activateDate == null || alarm.activateDate == currentDate)
                    listAns.add(alarm)
            }

            return@withContext listAns
        }

    suspend fun getAlarmFromDb(id: Long): AlarmSimpleData =
        withContext(Dispatchers.IO) {
            return@withContext alarmsDao.getAlarmById(id)
        }

    suspend fun getEarliestAlarmsFromDb(currentDate: ArrayList<String>): ArrayList<AlarmSimpleData?> =
        withContext(Dispatchers.IO) {
            val list = ArrayList<AlarmSimpleData?>()
            var listDay: ArrayList<AlarmSimpleData>
            var alarm: AlarmSimpleData?

            for (i in 0..6) {
                alarm = alarmsDao.getEarliestAlarm(i)
                if (alarm == null || (alarm.activateDate == null || alarm.activateDate == currentDate[i]) && alarm.isOn)
                    list.add(alarm)
                else {
                    listDay = getAlarmsFromDbByDayOfWeek(i, currentDate[i])

                    for (iAlarm in listDay) {
                        if (iAlarm.isOn) {
                            list.add(iAlarm)
                            break
                        }
                    }
                    if (list.size == i) list.add(null)
                }
            }
            return@withContext list
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
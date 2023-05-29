package com.example.smartalarm.data.repositories

import android.util.Log
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.data.constants.ALL_GAMES
import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.data.db.AlarmUserGamesData
import com.example.smartalarm.data.db.AlarmsDao
import com.example.smartalarm.data.db.GameData
import com.example.smartalarm.data.db.RecordsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlarmDbRepository(private val alarmsDao: AlarmsDao) {
    suspend fun getAlarmsFromDbByDayOfWeek(
        dayOfWeek: Int,
        currentDate: String
    ): ArrayList<AlarmData> =
        withContext(Dispatchers.IO) {
            val listDB = ArrayList(alarmsDao.getAlarmsByDay(dayOfWeek))
            val listAns = ArrayList<AlarmData>()

            for (alarm in listDB) {
                if (alarm.activateDate == null || alarm.activateDate == currentDate)
                    listAns.add(getAlarmWithGames(alarm.id))
            }

            return@withContext listAns
        }

    private suspend fun getAlarmFromDb(id: Long): AlarmSimpleData =
        withContext(Dispatchers.IO) {
            return@withContext alarmsDao.getAlarmById(id)
        }

    suspend fun getAlarmWithGames(id: Long): AlarmData =
        withContext(Dispatchers.IO) {
            val alarmSimpleData = getAlarmFromDb(id)
            val games = alarmsDao.getAllGames()
            val res: ArrayList<Int> = ArrayList()
            var con: AlarmUserGamesData?

            for (game in games) {
                con = alarmsDao.getGamesByAlarmAndGame(id, game.id)
                res.add(con?.difficulty ?: 0)
            }
            return@withContext AlarmData(alarmSimpleData, res)
        }

    suspend fun getAllAlarms(): ArrayList<AlarmData> =
        withContext(Dispatchers.IO) {
            val alarmsIds = alarmsDao.getAlarmsIds()
            val res = ArrayList<AlarmData>()

            for (id in alarmsIds)
                res.add(getAlarmWithGames(id))

            return@withContext res
        }

    suspend fun getEarliestAlarmsFromDb(currentDate: ArrayList<String>): ArrayList<AlarmSimpleData?> =
        withContext(Dispatchers.IO) {
            val list = ArrayList<AlarmSimpleData?>()
            var listDay: ArrayList<AlarmData>
            var alarm: AlarmSimpleData?

            for (i in 0..6) {
                alarm = alarmsDao.getEarliestAlarm(i)
                if (alarm == null || (alarm.activateDate == null || alarm.activateDate == currentDate[i]) && alarm.isOn)
                    list.add(alarm)
                else {
                    listDay = getAlarmsFromDbByDayOfWeek(i, currentDate[i])

                    for (iAlarm in listDay) {
                        if (iAlarm.isOn) {
                            list.add(AlarmSimpleData(iAlarm))
                            break
                        }
                    }
                    if (list.size == i) list.add(null)
                }
            }
            return@withContext list
        }

    suspend fun insertAlarmToDb(alarm: AlarmData) =
        withContext(Dispatchers.IO) {
            alarm.id = alarmsDao.insertNewAlarmData(AlarmSimpleData(alarm))
            insertAlarmGame(alarm)
        }

    suspend fun updateAlarmInDb(alarm: AlarmSimpleData) =
        withContext(Dispatchers.IO) {
            alarmsDao.updateAlarm(alarm)
        }

    suspend fun updateAlarmInDbWithGames(alarm: AlarmData) =
        withContext(Dispatchers.IO) {
            alarmsDao.updateAlarm(AlarmSimpleData(alarm))
            alarmsDao.deleteAlarmsGames(alarm.id)
            insertAlarmGame(alarm)
        }

    private suspend fun insertAlarmGame(alarm: AlarmData) =
        withContext(Dispatchers.IO) {
            for (i in 1..ALL_GAMES.size) {
                Log.i("alarm db", alarm.id.toString())
                if (alarm.gamesList[i - 1] != 0)
                    alarmsDao.insertNewAlarmUserGamesData(
                        AlarmUserGamesData(
                            idGame = i,
                            idAlarm = alarm.id,
                            difficulty = alarm.gamesList[i - 1]
                        )
                    )
            }
        }

    suspend fun deleteAlarmFromDb(alarm: AlarmSimpleData) =
        withContext(Dispatchers.IO) {
            alarmsDao.deleteAlarmsGames(alarm.id)
            alarmsDao.deleteAlarm(alarm)
        }

    suspend fun getGames(): List<GameData> =
        withContext(Dispatchers.IO) {
            return@withContext alarmsDao.getAllGames()
        }

    suspend fun getTopRecords(): ArrayList<RecordsData> =
        withContext(Dispatchers.IO) {
            val games = getGames()
            val res = ArrayList<RecordsData>()
            var record: RecordsData?
            for (game in games) {
                record = alarmsDao.getTopRecordOfGame(game.id)
                res.add(record ?: RecordsData(game))
            }
            return@withContext res
        }

    suspend fun insertRecord(record: RecordsData, alarm: AlarmData) =
        withContext(Dispatchers.IO) {
            alarm.recordScore = record.recordScore
            alarm.recordSeconds = record.recordTime
            alarmsDao.updateAlarm(AlarmSimpleData(alarm))

            alarmsDao.insertRecordData(record)
            alarmsDao.deleteOldRecords()
            val game = alarmsDao.getGameById(record.gameId)
            if (game.record == null || record.recordScore!! > game.record!!) {
                game.record = record.recordScore
                game.recordDate = record.date
                game.recordTime = record.recordTime
                alarmsDao.updateGame(game)
            }
        }

    suspend fun getRecordsByScore(): List<RecordsData> =
        withContext(Dispatchers.IO) {
            return@withContext alarmsDao.getRecords()
        }

    suspend fun getGameById(gameId: Int): GameData =
        withContext(Dispatchers.IO) {
            return@withContext alarmsDao.getGameById(gameId)
        }

    suspend fun updateRecord(recordsData: RecordsData) =
        withContext(Dispatchers.IO) {
            Log.i("grib", recordsData.recordShared.toString())
            alarmsDao.updateRecord(recordsData)
        }

    suspend fun deleteAllAlarms() =
        withContext(Dispatchers.IO) {
            alarmsDao.deleteAllUserGames()
            alarmsDao.deleteAllAlarms()
        }
}
package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.smartalarm.data.repositories.AlarmCreateRepository
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.data.constants.ALL_GAMES
import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.repositories.AlarmDbRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddAlarmFragmentViewModel(application: Application) : AndroidViewModel(application) {

    var currentAlarm: AlarmData? = null
    var gamesList: ArrayList<Int> = ArrayList()
    private val creator = AlarmCreateRepository(application.applicationContext)

    init {
        for (i in ALL_GAMES.indices)
            gamesList.add(1)
    }

    private val alarmDbRepository = AlarmDbRepository(
        AlarmsDB.getInstance(getApplication())?.alarmsDao()!!
    )

    suspend fun insertOrUpdateAlarmToDb(
        alarm: AlarmSimpleData
    ) = withContext(Dispatchers.IO) {
        if (currentAlarm == null) {

            alarmDbRepository.insertAlarmToDb(AlarmData(alarm, gamesList))
            AlarmData(alarm, gamesList).let(creator::create)
        }
        else {
            alarm.id = currentAlarm!!.alarmSimpleData.id
            alarm.recordSeconds = currentAlarm!!.alarmSimpleData.recordSeconds
            alarm.recordScore = currentAlarm!!.alarmSimpleData.recordScore
            alarmDbRepository.updateAlarmInDbWithGames(AlarmData(alarm, gamesList))
            AlarmData(alarm, gamesList).let(creator::update)
        }
    }

    suspend fun getAlarm(id: Long): AlarmData = withContext(Dispatchers.IO) {
        return@withContext alarmDbRepository.getAlarmWithGames(id)
    }
}
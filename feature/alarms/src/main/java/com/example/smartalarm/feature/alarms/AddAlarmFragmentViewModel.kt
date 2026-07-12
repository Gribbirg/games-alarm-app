package com.example.smartalarm.feature.alarms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.core.alarm.AlarmCreateRepository
import com.example.smartalarm.core.data.model.AlarmData
import com.example.smartalarm.core.data.constants.ALL_GAMES
import com.example.smartalarm.core.data.db.AlarmSimpleData
import com.example.smartalarm.core.data.db.AlarmsDB
import com.example.smartalarm.core.data.repositories.AlarmDbRepository
import com.example.smartalarm.core.data.repositories.isAhead
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    fun insertOrUpdateAlarm(alarm: AlarmSimpleData): Boolean {
        if (alarm.activateDate != null)
            if (!isAhead(alarm.activateDate!!, alarm.timeHour, alarm.timeMinute))
                return false
        insertOrUpdateAlarmToDb(alarm)
        return true
    }

    private fun insertOrUpdateAlarmToDb(
        alarm: AlarmSimpleData
    ) {
        viewModelScope.launch {
            if (alarm.name == "") alarm.name = "Будильник"

            if (currentAlarm == null) {

                alarm.id = alarmDbRepository.insertAlarmToDb(AlarmData(alarm, gamesList))
                AlarmData(alarm, gamesList).let(creator::create)
            } else {
                alarm.id = currentAlarm!!.id

                if (
                    currentAlarm!!.timeMinute != alarm.timeMinute ||
                    currentAlarm!!.timeHour != alarm.timeHour ||
                    currentAlarm!!.dayOfWeek != alarm.dayOfWeek ||
                    currentAlarm!!.activateDate != alarm.activateDate
                ) {
                    alarm.recordSeconds = null
                    alarm.recordScore = null
                } else {
                    alarm.recordSeconds = currentAlarm!!.recordSeconds
                    alarm.recordScore = currentAlarm!!.recordScore
                }
                alarmDbRepository.updateAlarmInDbWithGames(AlarmData(alarm, gamesList))
                AlarmData(alarm, gamesList).let(creator::update)
            }
        }
    }

    suspend fun getAlarm(id: Long): AlarmData = withContext(Dispatchers.IO) {
        return@withContext alarmDbRepository.getAlarmWithGames(id)
    }
}
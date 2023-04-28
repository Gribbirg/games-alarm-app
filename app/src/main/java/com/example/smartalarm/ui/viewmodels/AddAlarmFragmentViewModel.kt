package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.repositories.AlarmDbRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddAlarmFragmentViewModel(application: Application) : AndroidViewModel(application) {

    var currentAlarm: AlarmSimpleData? = null

    private val alarmDbRepository = AlarmDbRepository(
        AlarmsDB.getInstance(getApplication())?.alarmsDao()!!
    )

    suspend fun insertOrUpdateAlarmToDb(
        timeHour: Int,
        timeMinute: Int,
        dayOfWeek: Int,
        name: String,
        isVibration: Boolean,
        isRisingVolume: Boolean,
        activateDate: String?
    ) = withContext(Dispatchers.IO) {
        val alarm = AlarmSimpleData(
            timeHour = timeHour,
            timeMinute = timeMinute,
            dayOfWeek = dayOfWeek,
            name = name.ifEmpty { "Будильник" },
            activateDate = activateDate,
            isVibration = isVibration,
            isRisingVolume = isRisingVolume,
            recordScore = null,
            recordSeconds = null
        )
        if (currentAlarm == null)
            alarmDbRepository.insertAlarmToDb(alarm)
        else {
            alarm.id = currentAlarm!!.id
            alarm.recordSeconds = currentAlarm!!.recordSeconds
            alarm.recordScore = currentAlarm!!.recordScore
            alarmDbRepository.updateAlarmInDb(alarm)
        }
    }

    suspend fun getAlarm(id: Long): AlarmSimpleData = withContext(Dispatchers.IO) {
        return@withContext alarmDbRepository.getAlarmFromDb(id)
    }
}
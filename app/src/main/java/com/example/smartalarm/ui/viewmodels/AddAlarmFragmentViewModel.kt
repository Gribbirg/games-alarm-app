package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.data.db.AlarmsDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddAlarmFragmentViewModel(application: Application) : AndroidViewModel(application) {
    suspend fun insertAlarmToDb(
        timeHour: Int,
        timeMinute: Int,
        dayOfWeek:Int,
        name: String,
        isVibration: Boolean,
        isRisingVolume : Boolean,
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
        val dao = AlarmsDB.getInstance(getApplication())?.alarmsDao()
        dao?.insertNewAlarmData(alarm)
    }
}
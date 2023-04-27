package com.example.smartalarm.ui.viewmodels

import android.app.Application
import android.os.CombinedVibration
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.smartalarm.data.AlarmSimpleData
import com.example.smartalarm.data.AlarmsDB
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
            name =  name,
            activateDate = activateDate,
            isVibration = isVibration,
            isRisingVolume = isRisingVolume
        )
        val dao = AlarmsDB.getInstance(getApplication())?.alarmsDao()
        dao?.insertNewAlarmData(alarm)
    }
}
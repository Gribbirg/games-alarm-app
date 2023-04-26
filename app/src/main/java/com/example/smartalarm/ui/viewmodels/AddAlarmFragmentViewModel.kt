package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.smartalarm.data.AlarmSimpleData
import com.example.smartalarm.data.AlarmsDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddAlarmFragmentViewModel(application: Application) : AndroidViewModel(application) {
    suspend fun insertAlarmToDb(timeHour: Int, timeMinute: Int, dayOfWeek:Int, name: String)
    = withContext(Dispatchers.IO) {
        val alarm = AlarmSimpleData(
            timeHour = timeHour,
            timeMinute = timeMinute,
            dayOfWeek = dayOfWeek,
            recordMinutes =  0,
            recordSeconds =  0,
            name =  name,
            isOn =  false
        )
        val dao = AlarmsDB.getInstance(getApplication())?.alarmsDao()
        dao?.insertNewAlarmData(alarm)
    }
}
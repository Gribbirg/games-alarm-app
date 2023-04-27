package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.repositories.AlarmDbRepository
import com.example.smartalarm.data.repositories.CalendarRepository
import com.example.smartalarm.data.repositories.getTodayNumInWeek
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class AlarmsFragmentViewModel(application: Application) : AndroidViewModel(application) {

    var currentDayOfWeek: Int? = getTodayNumInWeek()
    private val calendarRepository = CalendarRepository()
    var weekCalendarData = calendarRepository.getWeek()

    var alarmsList: MutableLiveData<ArrayList<AlarmSimpleData>> = MutableLiveData()
    private val alarmDbRepository = AlarmDbRepository(
        AlarmsDB.getInstance(getApplication())?.alarmsDao()!!
    )

    suspend fun getAlarmsFromDbByDayOfWeek(dayOfWeek: Int) = withContext(Dispatchers.IO) {
        alarmsList.postValue(
            ArrayList(alarmDbRepository.getAlarmsFromDbByDayOfWeek(dayOfWeek))
        )
    }

    fun updateToday() {
        currentDayOfWeek = getTodayNumInWeek()
    }
    fun changeWeek(next: Int) {
        calendarRepository.changeWeek(next)
        weekCalendarData = calendarRepository.getWeek()
    }
}
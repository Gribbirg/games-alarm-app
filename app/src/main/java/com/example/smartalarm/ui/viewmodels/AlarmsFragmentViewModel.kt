package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.repositories.AlarmDbRepository
import com.example.smartalarm.data.repositories.CalendarRepository
import com.example.smartalarm.data.repositories.getDayOfWeekNameVinit
import com.example.smartalarm.data.repositories.getMontNameVinit
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

    fun getInfoLine() =
        if (currentDayOfWeek == null)
            "Выберите день"
        else
            "Будильники на ${getCurrentDateOfWeekString(currentDayOfWeek!!)},\n${
                getCurrentDateString(
                    currentDayOfWeek!!
                )
            }:"

    fun getCurrentDateString(dayOfWeek: Int) =
        weekCalendarData.daysList[dayOfWeek].dayNumber.toString() + " " +
                getMontNameVinit(weekCalendarData.daysList[dayOfWeek].monthNumber)

    fun getCurrentDateOfWeekString(dayOfWeek: Int) =
        if (weekCalendarData.daysList[dayOfWeek].today)
            "сегодня"
        else if (calendarRepository.isTomorrow(dayOfWeek))
            "завтра"
        else
            getDayOfWeekNameVinit(dayOfWeek)

    fun getCurrentDateStringForAllWeek(): ArrayList<String> {
        val list = ArrayList<String>()
        for (i in 0..6)
            list.add(getCurrentDateString(i))
        return list
    }

    fun getDateOfWeekStringForAllWeek(): ArrayList<String> {
        val list = ArrayList<String>()
        for (i in 0..6)
            list.add(getCurrentDateOfWeekString(i))
        return list
    }

}
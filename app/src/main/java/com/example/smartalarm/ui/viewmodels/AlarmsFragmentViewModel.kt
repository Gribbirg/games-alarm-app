package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

import com.example.smartalarm.data.AlarmSimpleData
import com.example.smartalarm.data.AlarmsDB
import com.example.smartalarm.data.WeekCalendarData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.util.*
import kotlin.collections.ArrayList

class AlarmsFragmentViewModel(application: Application) : AndroidViewModel(application) {

    var alarmsList: MutableLiveData<ArrayList<AlarmSimpleData>>
    var currentDayOfWeek: Int

    private val currentCalendar = Calendar.getInstance()
    lateinit var weekCalendarData: WeekCalendarData

    init {
        updateWeek()
        currentDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK)
        alarmsList = MutableLiveData()
    }

    suspend fun getAlarmsFromDb() = withContext(Dispatchers.IO) {

        val dao = AlarmsDB.getInstance(getApplication())?.alarmsDao()
        val list = dao?.getAlarms()?.let { ArrayList(it) }

        alarmsList.postValue(list!!)
    }

    suspend fun getAlarmsFromByDayOfWeek(dayOfWeek: Int) = withContext(Dispatchers.IO) {

        val dao = AlarmsDB.getInstance(getApplication())?.alarmsDao()
        val list = dao?.getAlarmsByDay(dayOfWeek)?.let { ArrayList(it) }

        alarmsList.postValue(list!!)
    }

//    fun updateAlarmToDb(alarm: AlarmSimpleData) {
//        val dao = AlarmsDB.getInstance(getApplication())?.alarmsDao()
//        dao?.updateAlarm(alarm)
//        getAlarmsFromDb()
//    }
//
//    fun deleteAlarmToDb(alarm: AlarmSimpleData) {
//        val dao = AlarmsDB.getInstance(getApplication())?.alarmsDao()
//        dao?.deleteAlarm(alarm)
//        getAlarmsFromDb()
//    }

    fun changeWeek(next: Int) {
        when (next) {
            1 -> currentCalendar.add(Calendar.WEEK_OF_YEAR, 1)
            -1 -> currentCalendar.add(Calendar.WEEK_OF_YEAR, -1)
        }
        updateWeek()
    }

    fun getTodayNumInWeek(): Int {
        val num = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return if (num == 1) 7 else num - 1
    }

    private fun updateWeek() {
        val calendar = currentCalendar
        weekCalendarData = WeekCalendarData()

        while (calendar.get(Calendar.DAY_OF_WEEK) != 2)
            calendar.add(Calendar.DATE, -1)

        do {
            weekCalendarData.addDate(
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR)
            )
            calendar.add(Calendar.DATE, 1)
        } while (calendar.get(Calendar.DAY_OF_WEEK) != 2)
        calendar.add(Calendar.DATE, -1)

        with(weekCalendarData) {
            daysList[5].isWeekend = true
            daysList[6].isWeekend = true
        }

        if (
            Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
            ==
            calendar.get(Calendar.WEEK_OF_YEAR)
        )
            weekCalendarData.daysList[getTodayNumInWeek() - 1].today = true

        val firstDayOfWeekMonth = weekCalendarData.daysList[0].getMonthName()
        val lastDayOfWeekMonth = weekCalendarData.daysList[6].getMonthName()

        with(weekCalendarData.monthList) {
            if (firstDayOfWeekMonth == lastDayOfWeekMonth) {
                add("")
                add(firstDayOfWeekMonth)
                add("")
            } else {
                add(firstDayOfWeekMonth)
                add("")
                add(lastDayOfWeekMonth)
            }
        }
    }
}
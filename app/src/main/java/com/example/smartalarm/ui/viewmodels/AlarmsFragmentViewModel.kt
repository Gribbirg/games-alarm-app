package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartalarm.data.AlarmSimpleData
import com.example.smartalarm.data.AlarmsDB
import com.example.smartalarm.data.AlarmsDao
import com.example.smartalarm.data.WeekCalendarData
import kotlinx.coroutines.*
import java.util.*

class AlarmsFragmentViewModel(
    private val dao : AlarmsDao,
    application: Application
) : AndroidViewModel(application) {

    private var job = Job()
    private val uiScope = CoroutineScope(
        Dispatchers.Main + job
    )
    private var alarmsList = MutableLiveData<List<AlarmSimpleData>>()

    private val currentCalendar = Calendar.getInstance()
    lateinit var weekCalendarData : WeekCalendarData

    init {
        updateWeek()
        initializeAlarms()
    }

    private fun initializeAlarms() {
        uiScope.launch {
            alarmsList.value = getAlarmsFromDatabase(
                currentCalendar.get(Calendar.DAY_OF_WEEK)
            )
        }
    }

    private suspend fun getAlarmsFromDatabase(dayOfWeek : Int) : List<AlarmSimpleData> {
        return dao.getAlarms(dayOfWeek)
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }

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
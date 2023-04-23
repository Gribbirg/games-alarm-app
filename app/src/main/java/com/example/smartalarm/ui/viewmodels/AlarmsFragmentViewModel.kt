package com.example.smartalarm.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.smartalarm.data.WeekCalendarData
import java.util.*

class AlarmsFragmentViewModel : ViewModel() {

    private val currentCalendar = Calendar.getInstance()
    lateinit var weekCalendarData : WeekCalendarData

    init {
        updateWeek()
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
package com.example.smartalarm.data.repositories

import android.util.Log
import com.example.smartalarm.data.data.WeekCalendarData
import com.example.smartalarm.data.db.AlarmSimpleData
import java.util.Calendar

val holidays: ArrayList<ArrayList<Int>> = arrayListOf(
    arrayListOf(0, 2),
    arrayListOf(0, 3),
    arrayListOf(0, 4),
    arrayListOf(0, 5),
    arrayListOf(0, 6),
    arrayListOf(1, 23),
    arrayListOf(1, 24),
    arrayListOf(2, 8),
    arrayListOf(2, 8),
    arrayListOf(4, 1),
    arrayListOf(4, 8),
    arrayListOf(4, 9),
    arrayListOf(5, 12),
    arrayListOf(10, 6)
)

class CalendarRepository {
    private var currentCalendar = Calendar.getInstance()

    fun changeWeek(next: Int) {
        currentCalendar.add(Calendar.WEEK_OF_YEAR, next)
    }

    fun isToday() = currentCalendar == Calendar.getInstance()

    fun isAhead(dayOfWeek: Int, howMuchAhead: Int): Boolean {
        val calendar = currentCalendar
        while (calendar.get(Calendar.DAY_OF_WEEK) != 2)
            calendar.add(Calendar.DATE, -1)
        while ((calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 != dayOfWeek) {
            calendar.add(Calendar.DATE, 1)
        }

        calendar.add(Calendar.DATE, -howMuchAhead)
        val saveData = calendar.get(Calendar.DATE)
        calendar.add(Calendar.DATE, howMuchAhead)
        return saveData == Calendar.getInstance().get(Calendar.DATE) &&
                currentCalendar.get(Calendar.YEAR) == getCurrentYear()

    }

    fun getMonthOfDay(dayOfWeek: Int): Int {
        val calendar = currentCalendar
        while (calendar.get(Calendar.DAY_OF_WEEK) != 1)
            calendar.add(Calendar.DATE, -1)
        while ((calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 != dayOfWeek) {
            calendar.add(Calendar.DATE, 1)
        }
        return calendar.get(Calendar.MONTH)
    }

    fun getWeek(): WeekCalendarData {
        val calendar = currentCalendar
        val weekCalendarData = WeekCalendarData(currentCalendar.get(Calendar.WEEK_OF_YEAR))

        while (calendar.get(Calendar.DAY_OF_WEEK) != 2)
            calendar.add(Calendar.DATE, -1)

        do {
            weekCalendarData.addDate(
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR)
            )

            for (holiday in holidays) {

                if (
                    holiday[0] == calendar.get(Calendar.MONTH) &&
                    holiday[1] == calendar.get(Calendar.DAY_OF_MONTH)
                )
                    weekCalendarData.daysList[weekCalendarData.getListLen() - 1].isHoliday = true

            }

            calendar.add(Calendar.DATE, 1)

        } while (calendar.get(Calendar.DAY_OF_WEEK) != 2)
        calendar.add(Calendar.DATE, -1)


        with(weekCalendarData) {
            daysList[5].isWeekend = true
            daysList[6].isWeekend = true
        }

        if (
            Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) ==
            calendar.get(Calendar.WEEK_OF_YEAR) &&
            calendar.get(Calendar.YEAR) == getCurrentYear()
        )
            weekCalendarData.daysList[getTodayNumInWeek()].today = true

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

        return weekCalendarData
    }

    fun getDateOfCurrentWeekString(dayOfWeek: Int): String {
        val calendar = currentCalendar
        while (calendar.get(Calendar.DAY_OF_WEEK) != 2)
            calendar.add(Calendar.DATE, -1)
        while ((calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 != dayOfWeek) {
            calendar.add(Calendar.DATE, 1)
        }
        return "${currentCalendar.get(Calendar.DAY_OF_MONTH)}." +
                "${currentCalendar.get(Calendar.MONTH) + 1}." +
                "${currentCalendar.get(Calendar.YEAR)}"
    }

    fun getWeekOfDay(weekOfYear: Int, year: Int): WeekCalendarData {

        while (currentCalendar.get(Calendar.YEAR) > year)
            currentCalendar.add(Calendar.YEAR, -1)
        while (currentCalendar.get(Calendar.YEAR) < year)
            currentCalendar.add(Calendar.YEAR, 1)

        while (currentCalendar.get(Calendar.WEEK_OF_YEAR) > weekOfYear)
            currentCalendar.add(Calendar.WEEK_OF_YEAR, -1)
        while (currentCalendar.get(Calendar.WEEK_OF_YEAR) < weekOfYear)
            currentCalendar.add(Calendar.WEEK_OF_YEAR, 1)

        return getWeek()
    }
}

fun getNearestDate(dayOfWeek: Int): ArrayList<Int> {
    val calendar = Calendar.getInstance()
    val res = ArrayList<Int>()

    while ((calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 != dayOfWeek) {
        calendar.add(Calendar.DATE, 1)
    }

    return arrayListOf(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}
fun getTodayNumInWeek(): Int {
    return (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
}

fun getMontNameVinit(monthNum: Int) = when (monthNum) {
    1 -> "января"
    2 -> "февраля"
    3 -> "марта"
    4 -> "апреля"
    5 -> "мая"
    6 -> "июня"
    7 -> "июля"
    8 -> "августа"
    9 -> "сентября"
    10 -> "октября"
    11 -> "ноября"
    12 -> "декабря"
    else -> ""
}

fun getDayOfWeekNameVinit(dayOfWeek: Int) = when (dayOfWeek) {
    0 -> "понедельник"
    1 -> "вторник"
    2 -> "среду"
    3 -> "четверг"
    4 -> "пятницу"
    5 -> "субботу"
    6 -> "воскресенье"
    else -> ""
}

fun getCurrentYear() = Calendar.getInstance().get(Calendar.YEAR)

fun timesToString(alarmsList: ArrayList<AlarmSimpleData?>): ArrayList<String> {
    val list = ArrayList<String>()
    for (alarm in alarmsList)
        list.add(
            if (alarm == null) ""
            else if (alarm.timeMinute < 10) "${alarm.timeHour}:0${alarm.timeMinute}"
            else "${alarm.timeHour}:${alarm.timeMinute}"
        )
    return list
}

fun checkForHoliday() : Int {
    val calendar = Calendar.getInstance()
    for (i in 1..2) {
        for (holiday in holidays) {

            if (
                holiday[0] == calendar.get(Calendar.MONTH) &&
                holiday[1] == calendar.get(Calendar.DAY_OF_MONTH)
            )
                return i


        }
        calendar.add(Calendar.DATE, 1)
    }
    return 0
}
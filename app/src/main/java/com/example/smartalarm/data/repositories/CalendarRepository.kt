package com.example.smartalarm.data.repositories

import android.util.Log
import com.example.smartalarm.data.constants.HOLIDAYS
import com.example.smartalarm.data.data.Date
import com.example.smartalarm.data.data.WeekCalendarData
import com.example.smartalarm.data.db.AlarmSimpleData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

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
                calendar.get(Calendar.YEAR),
                (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
            )

            for (holiday in HOLIDAYS) {

                if (
                    holiday[0] == calendar.get(Calendar.MONTH) &&
                    holiday[1] == calendar.get(Calendar.DAY_OF_MONTH) &&
                    holiday[2] == calendar.get(Calendar.YEAR)
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

    companion object {
        fun isAhead(day: Date): CalendarIsAhead {
            val calendar = Calendar.getInstance()
            if (Date(calendar) == day)
                return CalendarIsAhead.TODAY
            calendar.add(Calendar.DATE, 1)
            if (Date(calendar) == day)
                return CalendarIsAhead.TOMORROW
            calendar.add(Calendar.DATE, 1)
            if (Date(calendar) == day)
                return CalendarIsAhead.AFTER_TOMORROW
            return CalendarIsAhead.FAR
        }

        fun getWeek(day: Date): List<Date> {
            val calendar = calendarFromDate(day)

            while (calendar.get(Calendar.DAY_OF_WEEK) != 2)
                calendar.add(Calendar.DATE, -1)

            val res = ArrayList<Date>()
            do {
                res.add(Date(calendar))
                calendar.add(Calendar.DATE, 1)
            } while (calendar.get(Calendar.DAY_OF_WEEK) != 2)
            return res
        }

        private fun calendarFromDate(date: Date) = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, date.dayNumber)
            set(Calendar.MONTH, date.monthNumber - 1)
            set(Calendar.YEAR, date.yearNumber)
        }

        fun addDays(date: Date, count: Int): Date {
            val calendar = calendarFromDate(date)
            calendar.add(Calendar.DATE, count)
            return Date(calendar)
        }

        fun getDateByMillis(dateByMillis: Long) =
            Calendar.getInstance().apply {
                timeInMillis = dateByMillis
            }.toDateString()

        fun getTodayInMillis() =
            Calendar
                .getInstance()
                .also {
                    it.set(Calendar.HOUR_OF_DAY, 0)
                    it.set(Calendar.MINUTE, 0)
                    it.set(Calendar.SECOND, 0)
                    it.set(Calendar.MILLISECOND, 0)
                }
                .timeInMillis

        fun getDateAhead(daysAhead: Int) =
            Calendar.getInstance().also { it.add(Calendar.DATE, daysAhead) }.toDateString()
    }
}

fun Calendar.toDateString() =
    "${this.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')}." +
            "${(this.get(Calendar.MONTH) + 1).toString().padStart(2, '0')}." +
            "${this.get(Calendar.YEAR)}"

enum class CalendarIsAhead {
    TODAY,
    TOMORROW,
    AFTER_TOMORROW,
    FAR;

    override fun toString(): String = when (this) {
        TODAY -> "сегодня"
        TOMORROW -> "завтра"
        AFTER_TOMORROW -> "послезавтра"
        FAR -> ""
    }
}

fun isAhead(date: String, hour: Int, minute: Int): Boolean {
    val currentCalendar = Calendar.getInstance()
    val dateList = date.split('.')
    return if (dateList[2].toInt() > currentCalendar.get(Calendar.YEAR))
        true
    else if (dateList[2].toInt() < currentCalendar.get(Calendar.YEAR))
        false
    else if (dateList[1].toInt() > currentCalendar.get(Calendar.MONTH) + 1)
        true
    else if (dateList[1].toInt() < currentCalendar.get(Calendar.MONTH) + 1)
        false
    else if (dateList[0].toInt() > currentCalendar.get(Calendar.DAY_OF_MONTH))
        true
    else if (dateList[0].toInt() < currentCalendar.get(Calendar.DAY_OF_MONTH))
        false
    else if (hour > currentCalendar.get(Calendar.HOUR_OF_DAY))
        true
    else minute > currentCalendar.get(Calendar.MINUTE)
}

fun getToday(): Date = Date(Calendar.getInstance())

fun getTodayDate(): String {
    return Calendar.getInstance().toDateString()
}

fun getNearestDate(dayOfWeek: Int, timeMinute: Int, timeHours: Int): ArrayList<Int> {
    val calendar = Calendar.getInstance()

    if (
        calendar.get(Calendar.HOUR_OF_DAY) > timeHours ||
        calendar.get(Calendar.HOUR_OF_DAY) == timeHours && calendar.get(Calendar.MINUTE) >= timeMinute
    )
        calendar.add(Calendar.DATE, 1)
    Log.i("calendar", 'h' + calendar.get(Calendar.HOUR_OF_DAY).toString())
    Log.i("calendar", 'm' + calendar.get(Calendar.MINUTE).toString())
    while ((calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 != dayOfWeek) {
        calendar.add(Calendar.DATE, 1)
    }
    Log.i("calendar", "Alarm Date:" + calendar.get(Calendar.DATE).toString())
    return arrayListOf(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}

fun dateListFromString(string: String): ArrayList<Int> {
    val list = string.split('.')
    return arrayListOf(list[2].toInt(), list[1].toInt(), list[0].toInt())
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

fun getMontName(monthNum: Int) = when (monthNum) {
    1 -> "Январь"
    2 -> "Февраль"
    3 -> "Март"
    4 -> "Апрель"
    5 -> "Май"
    6 -> "Июнь"
    7 -> "Июль"
    8 -> "Август"
    9 -> "Сентябрь"
    10 -> "Октябрь"
    11 -> "Ноябрь"
    12 -> "Декабрь"
    else -> ""
}

fun getDayOfWeekName(dayOfWeek: Int) = when (dayOfWeek) {
    0 -> "понедельник"
    1 -> "вторник"
    2 -> "среда"
    3 -> "четверг"
    4 -> "пятница"
    5 -> "суббота"
    6 -> "воскресенье"
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

fun checkForHoliday(): Int {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DATE, 1)
    for (i in 1..2) {
        for (holiday in HOLIDAYS) {

            if (
                holiday[0] == calendar.get(Calendar.MONTH) &&
                holiday[1] == calendar.get(Calendar.DAY_OF_MONTH) &&
                holiday[2] == calendar.get(Calendar.YEAR)
            )
                return i


        }
        Log.i("grib", calendar.get(Calendar.MONTH).toString())
        Log.i("grib", calendar.get(Calendar.DAY_OF_MONTH).toString())
        Log.i("grib", calendar.get(Calendar.YEAR).toString())
        calendar.add(Calendar.DATE, 1)
    }
    return 0
}

fun getCurrentTimeString(): String {
    val currentDateTime = LocalDateTime.now()
    return currentDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
}

fun getCurrentDateString(): String {
    val calendar = Calendar.getInstance()
    return "${getMontName(calendar.get(Calendar.MONTH) + 1)} " +
            "${calendar.get(Calendar.DAY_OF_MONTH)}, " +
            getDayOfWeekName((calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7)
}

fun getDefaultWeekDataList(size: Int = 100): List<WeekCalendarData> {
    val rep = CalendarRepository()
    val res = ArrayList<WeekCalendarData>()
    repeat(size) {
        res.add(rep.getWeek())
        rep.changeWeek(1)
    }
    return res
}

fun getDayOfWeekShortName(dayOfWeek: Int) = when (dayOfWeek) {
    0 -> "Пн"
    1 -> "Вт"
    2 -> "Ср"
    3 -> "Чт"
    4 -> "Пт"
    5 -> "Сб"
    6 -> "Вс"
    else -> ""
}
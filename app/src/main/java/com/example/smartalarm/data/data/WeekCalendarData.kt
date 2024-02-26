package com.example.smartalarm.data.data

import android.util.Log

class WeekCalendarData(val weekOfYear: Int) {
    val daysList = ArrayList<DateUnit>()
    val monthList = ArrayList<String>()

    open class Date(
        var dayNumber: Int,
        var monthNumber: Int,
        var yearNumber: Int,


    ) {
        override fun equals(other: Any?): Boolean {

            if (this === other) return true
            if (other !is Date) {
                return false
            }

            if (dayNumber != other.dayNumber) return false
            if (monthNumber != other.monthNumber) return false
            return yearNumber == other.yearNumber
        }

        override fun hashCode(): Int {
            var result = dayNumber
            result = 31 * result + monthNumber
            result = 31 * result + yearNumber
            return result
        }

        override fun toString(): String {
            return "${dayNumber}.${monthNumber}.${yearNumber}"
        }
    }

    class DateUnit(
        dayNumber: Int,
        monthNumber: Int,
        yearNumber: Int,
        var today: Boolean = false,
        var isWeekend: Boolean = false,
        var isHoliday: Boolean = false
    ) : Date(
        dayNumber,
        monthNumber,
        yearNumber,
    ) {

        fun getMonthName(): String =
            when (monthNumber) {
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
    }

    fun addDate(
        _dayNumber: Int,
        _monthNumber: Int,
        _yearNumber: Int,
        _today: Boolean = false,
        _isWeekend: Boolean = false,
        _isHoliday: Boolean = false
    ) {
        daysList.add(
            DateUnit(
                _dayNumber,
                _monthNumber,
                _yearNumber,
                _today,
                _isWeekend,
                _isHoliday
            )
        )
    }

    fun getListLen() = daysList.size

    fun toStringArray(): ArrayList<String> {
        val res = ArrayList<String>()
        for (date in daysList)
            res.add(date.toString())
        return res
    }

    override fun toString(): String {
        return "$daysList $monthList"
    }

    companion object {
        fun getDefault(): WeekCalendarData = WeekCalendarData(1).apply {
            addDate(1, 1, 2024, true)
            addDate(2, 1, 2024)
            addDate(3, 1, 2024)
            addDate(4, 1, 2024)
            addDate(5, 1, 2024, _isHoliday = true)
            addDate(6, 1, 2024, _isWeekend = true)
            addDate(7, 1, 2024, _isWeekend = true)

            monthList.add("Январь")
            monthList.add("")
            monthList.add("Февраль")
        }

        fun getDefaultList(size: Int = 10): List<WeekCalendarData> = List(size) {
            getDefault()
        }
    }
}


package com.example.smartalarm.data.data

class WeekCalendarData(val weekOfYear: Int) {
    val daysList = ArrayList<DateUnit>()
    val monthList = ArrayList<String>()

    class DateUnit(var dayNumber: Int,
                   var monthNumber: Int,
                   var yearNumber: Int,
                   var today: Boolean = false,
                   var isWeekend: Boolean= false,
                   var isHoliday: Boolean = false
    ) {

        fun getMonthName() : String =
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

        override fun toString(): String {
            return "${dayNumber}.${monthNumber}.${yearNumber}"
        }
    }

    fun addDate(_dayNumber: Int,
                _monthNumber: Int,
                _yearNumber: Int,
                _today: Boolean = false,
                _isWeekend: Boolean= false,
                _isHoliday: Boolean = false)
    {
        daysList.add(DateUnit(_dayNumber, _monthNumber, _yearNumber, _today, _isWeekend, _isHoliday))
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
}
package com.example.smartalarm

class WeekCalendarData {
    val daysList = ArrayList<DateUnit>()
    val monthList = ArrayList<String>()

    class DateUnit(_dayNumber: Int,
                   _monthNumber: Int,
                   _today: Boolean = false,
                   _isWeekend: Boolean= false,
                   _isHoliday: Boolean = false
    ) {

        var dayNumber = _dayNumber
        var monthNumber = _monthNumber
        var today: Boolean = _today
        var isWeekend: Boolean= _isWeekend
        var isHoliday: Boolean = _isHoliday

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
    }

    fun addDate(_dayNumber: Int,
                _monthNumber: Int,
                _today: Boolean = false,
                _isWeekend: Boolean= false,
                _isHoliday: Boolean = false)
    {
        daysList.add(DateUnit(_dayNumber, _monthNumber, _today, _isWeekend, _isHoliday))
    }
}
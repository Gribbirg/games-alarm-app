package com.example.smartalarm

class DateUnit(_dayNumber: Int,
               _monthNumber: Int,
               _today: Boolean = false,
               _isWeekend: Boolean= false,
               _isHoliday: Boolean = false
) {

    val dayNumber = _dayNumber
    val monthNumber = _monthNumber
    val today: Boolean = _today
    val isWeekend: Boolean= _isWeekend
    val isHoliday: Boolean = _isHoliday

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
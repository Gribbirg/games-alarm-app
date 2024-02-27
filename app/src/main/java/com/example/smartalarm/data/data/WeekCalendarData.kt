package com.example.smartalarm.data.data

class WeekCalendarData(val weekOfYear: Int) {
    val daysList = ArrayList<DateUnit>()
    val monthList = ArrayList<String>()

    class DateUnit(
        dayNumber: Int,
        monthNumber: Int,
        yearNumber: Int,
        dayOfWeek: Int,
        var today: Boolean = false,
        var isWeekend: Boolean = false,
        var isHoliday: Boolean = false
    ) : Date(
        dayNumber,
        monthNumber,
        yearNumber,
        dayOfWeek
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
        _dayOfWeek: Int,
        _today: Boolean = false,
        _isWeekend: Boolean = false,
        _isHoliday: Boolean = false
    ) {
        daysList.add(
            DateUnit(
                _dayNumber,
                _monthNumber,
                _yearNumber,
                _dayOfWeek,
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
            addDate(1, 1, 2024, 0, true)
            addDate(2, 1, 2024, 1)
            addDate(3, 1, 2024, 2)
            addDate(4, 1, 2024, 3)
            addDate(5, 1, 2024, 4, _isHoliday = true)
            addDate(6, 1, 2024, 5, _isWeekend = true)
            addDate(7, 1, 2024, 6, _isWeekend = true)

            monthList.add("Январь")
            monthList.add("")
            monthList.add("Февраль")
        }

        fun getDefaultList(size: Int = 10): List<WeekCalendarData> = List(size) {
            getDefault()
        }
    }
}


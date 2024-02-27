package com.example.smartalarm.data.data

import java.util.Calendar

open class Date(
    var dayNumber: Int,
    var monthNumber: Int,
    var yearNumber: Int,
    var dayOfWeek: Int
) {

    constructor(calendar: Calendar) : this(
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.YEAR),
        (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    )

    override fun equals(other: Any?): Boolean {

        if (this === other) return true
        if (other !is Date) {
            return false
        }

        if (dayNumber != other.dayNumber) return false
        if (monthNumber != other.monthNumber) return false
        if (dayOfWeek != other.dayOfWeek) return false
        return yearNumber == other.yearNumber
    }

    override fun hashCode(): Int {
        var result = dayNumber
        result = 31 * result + monthNumber
        result = 31 * result + yearNumber
        return result
    }

    override fun toString(): String = "$dayNumber.$monthNumber.$yearNumber; weekday: $dayOfWeek"
}
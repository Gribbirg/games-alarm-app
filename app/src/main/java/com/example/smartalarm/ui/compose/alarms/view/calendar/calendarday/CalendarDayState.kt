package com.example.smartalarm.ui.compose.alarms.view.calendar.calendarday

import com.example.smartalarm.data.data.WeekCalendarData

data class CalendarDayState (
    val data: WeekCalendarData.DateUnit,
    val num: Int,
    val earliestAlarm: String,
    var isSelected: Boolean = false
)
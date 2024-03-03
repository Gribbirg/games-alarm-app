package com.example.smartalarm.ui.compose.alarms.view.calendar

import com.example.smartalarm.data.data.WeekCalendarData

data class CalendarViewState(
    val data: List<WeekCalendarData>,
    val selectedDayNum: Int
)
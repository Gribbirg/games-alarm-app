package com.example.smartalarm.ui.compose.alarms.view.calendar

import com.example.smartalarm.ui.compose.alarms.view.calendarday.CalendarDayState

data class CalendarViewState(
    val days: List<List<CalendarDayState>>,
    val monthTextList: List<List<String>>,
    val selectedDayNum: Int
)
package com.example.smartalarm.ui.compose.alarms

import com.example.smartalarm.data.data.Date
import com.example.smartalarm.data.data.WeekCalendarData

data class AlarmsState(
    val weekCalendarData: List<WeekCalendarData>,
    var selectedDay: Date,
    var dayInfoText: String
)

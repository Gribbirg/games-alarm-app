package com.example.smartalarm.ui.compose.alarms.view.calendar.calendarday

import com.example.smartalarm.ui.compose.alarms.AlarmsEvent
import com.example.smartalarm.ui.compose.alarms.view.calendar.CalendarViewEvent

open class CalendarDayEvent : CalendarViewEvent() {
}

data class CalendarDayOnClickEvent(val dayNum: Int) : CalendarDayEvent()
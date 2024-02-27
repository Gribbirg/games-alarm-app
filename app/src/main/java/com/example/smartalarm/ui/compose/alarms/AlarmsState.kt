package com.example.smartalarm.ui.compose.alarms

import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.data.data.Date
import com.example.smartalarm.data.data.WeekCalendarData

data class AlarmsState(
    val weekCalendarData: List<WeekCalendarData>,
    var selectedDayNum: Int,
    var dayInfoText: String,
    var alarmsListState: AlarmsListState
)

abstract class AlarmsListState

class AlarmsListLoadingState : AlarmsListState()

class AlarmsListLoadedState(
    val alarmsList: List<List<AlarmData>>
) : AlarmsListState()

class AlarmsListErrorState(
    val text: String
) : AlarmsListState()
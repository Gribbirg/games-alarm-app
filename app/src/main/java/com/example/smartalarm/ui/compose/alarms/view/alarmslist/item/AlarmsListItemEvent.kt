package com.example.smartalarm.ui.compose.alarms.view.alarmslist.item

import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.AlarmsListEvent

abstract class AlarmsListItemEvent(val alarm: AlarmData) : AlarmsListEvent()

class AlarmsListItemSetOnStateEvent(alarm: AlarmData, val isOn: Boolean) : AlarmsListItemEvent(
    alarm
)

class AlarmsListItemChangeEvent(alarm: AlarmData) : AlarmsListItemEvent(alarm)
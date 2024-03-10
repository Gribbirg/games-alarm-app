package com.example.smartalarm.ui.compose.view.alarmitem

import com.example.smartalarm.data.data.AlarmData

abstract class AlarmItemEvent(val alarm: AlarmData)

class AlarmItemSetOnStateEvent(alarm: AlarmData, val isOn: Boolean) : AlarmItemEvent(
    alarm
)

class AlarmItemChangeEvent(alarm: AlarmData) : AlarmItemEvent(alarm)


class AlarmItemClockClickedEvent(alarm: AlarmData) : AlarmItemEvent(alarm)
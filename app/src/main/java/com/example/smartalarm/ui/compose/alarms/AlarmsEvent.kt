package com.example.smartalarm.ui.compose.alarms

import com.example.smartalarm.data.data.AlarmData

open class AlarmsEvent

abstract class SnackBarEvent : AlarmsEvent()

data class SnackBarAlarmReturnEvent(val alarm: AlarmData) : SnackBarEvent()

class SnackBarDismissEvent : SnackBarEvent()
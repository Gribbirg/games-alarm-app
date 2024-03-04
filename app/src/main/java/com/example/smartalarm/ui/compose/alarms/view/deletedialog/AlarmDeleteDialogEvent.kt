package com.example.smartalarm.ui.compose.alarms.view.deletedialog

import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.ui.compose.alarms.AlarmsEvent

abstract class AlarmDeleteDialogEvent : AlarmsEvent()

class AlarmDeleteDialogConfirmEvent(val alarm: AlarmData) : AlarmDeleteDialogEvent()

class AlarmDeleteDialogDismissEvent : AlarmDeleteDialogEvent()
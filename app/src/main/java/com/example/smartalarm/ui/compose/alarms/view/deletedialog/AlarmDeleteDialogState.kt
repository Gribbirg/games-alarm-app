package com.example.smartalarm.ui.compose.alarms.view.deletedialog

import com.example.smartalarm.data.data.AlarmData

abstract class AlarmDeleteDialogState

data class AlarmDeleteDialogOnState(
    val alarm: AlarmData,
) : AlarmDeleteDialogState()

class AlarmDeleteDialogOffState : AlarmDeleteDialogState()
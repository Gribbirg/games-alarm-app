package com.example.smartalarm.ui.compose.view.timepickerdialog

import com.example.smartalarm.data.data.AlarmData

abstract class TimePickerDialogState

class TimePickerDialogOffState : TimePickerDialogState()

data class TimePickerDialogOnState(
    val alarm: AlarmData
) : TimePickerDialogState()
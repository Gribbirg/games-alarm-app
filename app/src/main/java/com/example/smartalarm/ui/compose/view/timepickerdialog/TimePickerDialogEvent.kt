package com.example.smartalarm.ui.compose.view.timepickerdialog

import com.example.smartalarm.data.data.AlarmData

abstract class TimePickerDialogEvent

class TimePickerDialogDismissEvent: TimePickerDialogEvent()

data class TimePickerDialogSetEvent(
    val hour: Int,
    val minute: Int,
    val alarm: AlarmData
) : TimePickerDialogEvent()
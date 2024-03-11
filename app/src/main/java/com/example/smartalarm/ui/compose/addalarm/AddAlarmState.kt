package com.example.smartalarm.ui.compose.addalarm

import androidx.compose.material3.TimePickerState
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogState

data class AddAlarmState(
    val alarm: AlarmData = AlarmData(),
    val isNew: Boolean,
    val daysOfWeek: MutableList<Boolean>,
    val saveFinish: Boolean = false,

    val timePickerDialogState: TimePickerDialogState,
    val alertDialogState: AddAlarmAlertDialogState
) {}

abstract class AddAlarmAlertDialogState

class AddAlarmAlertDialogOffState : AddAlarmAlertDialogState()

class AddAlarmAlertDialogDaysNotSelectedState : AddAlarmAlertDialogState()
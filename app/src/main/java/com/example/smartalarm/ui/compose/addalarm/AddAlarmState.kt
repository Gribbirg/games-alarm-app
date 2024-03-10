package com.example.smartalarm.ui.compose.addalarm

import androidx.compose.material3.TimePickerState
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogState

data class AddAlarmState(
    val alarm: AlarmData = AlarmData(),
    val isNew: Boolean,
    val daysOfWeek: MutableList<Boolean>,

    val timePickerDialogState: TimePickerDialogState
) {}
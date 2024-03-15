package com.example.smartalarm.ui.compose.addalarm

import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.data.data.GameData
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogOffState
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogState

data class AddAlarmState(
    val alarm: AlarmData = AlarmData(),
    val isNew: Boolean,
    val daysOfWeek: MutableList<Boolean>,
    val selectedGamesList: List<GameData>,
    val saveFinish: Boolean = false,
    val hasCopiedAlarm: Boolean = false,

    val timePickerDialogState: TimePickerDialogState = TimePickerDialogOffState(),
    val alertDialogState: AddAlarmAlertDialogState = AddAlarmAlertDialogOffState(),
    val snackBarState: AddAlarmSnackBarState = AddAlarmSnackBarOffState()
) {}

abstract class AddAlarmAlertDialogState

class AddAlarmAlertDialogOffState : AddAlarmAlertDialogState()

class AddAlarmAlertDialogDaysNotSelectedState : AddAlarmAlertDialogState()


abstract class AddAlarmSnackBarState

class AddAlarmSnackBarOffState : AddAlarmSnackBarState()

data class AddAlarmSnackBarPastedState(
    val alarm: AlarmData
) : AddAlarmSnackBarState()

class AddAlarmSnackBarNothingToPastedState : AddAlarmSnackBarState()
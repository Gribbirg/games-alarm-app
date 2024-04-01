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
    val isOneTime: Boolean = false,

    val timePickerDialogState: TimePickerDialogState = TimePickerDialogOffState(),
    val alertDialogState: AddAlarmAlertDialogState = AddAlarmAlertDialogOffState(),
    val snackBarState: AddAlarmSnackBarState = AddAlarmSnackBarOffState(),
    val datePickerState: AddAlarmDatePickerState = AddAlarmDatePickerOffState()
)

abstract class AddAlarmAlertDialogState

class AddAlarmAlertDialogOffState : AddAlarmAlertDialogState()

data class AddAlarmAlertDialogTextState(
    val head: String,
    val body: String
) : AddAlarmAlertDialogState()

abstract class AddAlarmSnackBarState

class AddAlarmSnackBarOffState : AddAlarmSnackBarState()

data class AddAlarmSnackBarPastedState(
    val alarm: AlarmData
) : AddAlarmSnackBarState()

data class AddAlarmSnackBarTextAlertState (
    val text: String
) : AddAlarmSnackBarState()


abstract class AddAlarmDatePickerState

class AddAlarmDatePickerOnState : AddAlarmDatePickerState()

class AddAlarmDatePickerOffState : AddAlarmDatePickerState()
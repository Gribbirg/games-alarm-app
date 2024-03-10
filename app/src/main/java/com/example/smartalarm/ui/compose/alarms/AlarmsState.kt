package com.example.smartalarm.ui.compose.alarms

import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.AlarmsListState
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetState
import com.example.smartalarm.ui.compose.alarms.view.calendar.CalendarViewState
import com.example.smartalarm.ui.compose.alarms.view.deletedialog.AlarmDeleteDialogState
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogState

open class AlarmsState(
    val alarmsListState: AlarmsListState,
    val calendarViewState: CalendarViewState,
    val bottomSheetState: AlarmEditBottomSheetState,
    val deleteDialogState: AlarmDeleteDialogState,
    val alarmsSnackBarState: AlarmsSnackBarState,
    val timePickerState: TimePickerDialogState,
    val dayInfoText: String
) {
    fun copy(
        alarmsListState: AlarmsListState = this.alarmsListState,
        calendarViewState: CalendarViewState = this.calendarViewState,
        bottomSheetState: AlarmEditBottomSheetState = this.bottomSheetState,
        deleteDialogState: AlarmDeleteDialogState = this.deleteDialogState,
        alarmsSnackBarState: AlarmsSnackBarState = this.alarmsSnackBarState,
        timePickerState: TimePickerDialogState = this.timePickerState,
        dayInfoText: String = this.dayInfoText
    ) = AlarmsState(
        alarmsListState,
        calendarViewState,
        bottomSheetState,
        deleteDialogState,
        alarmsSnackBarState,
        timePickerState,
        dayInfoText
    )
}

abstract class AlarmsSnackBarState

class AlarmsSnackBarOffState : AlarmsSnackBarState()

data class AlarmsSnackBarAlarmDeleteState(
    val alarm: AlarmData
) : AlarmsSnackBarState()

data class AlarmsSnackBarAlarmCopyState(
    val alarm: AlarmData
) : AlarmsSnackBarState()

data class AlarmsSnackBarTimeChangeState(
    val alarm: AlarmData
) : AlarmsSnackBarState()

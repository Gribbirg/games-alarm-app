package com.example.smartalarm.ui.compose.alarms

import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.AlarmsListState
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetState
import com.example.smartalarm.ui.compose.alarms.view.calendar.CalendarViewState
import com.example.smartalarm.ui.compose.alarms.view.deletedialog.AlarmDeleteDialogState

open class AlarmsState(
    val alarmsListState: AlarmsListState,
    val calendarViewState: CalendarViewState,
    val bottomSheetState: AlarmEditBottomSheetState,
    val deleteDialogState: AlarmDeleteDialogState,
    val snackBarState: SnackBarState,
    val dayInfoText: String
) {
    fun copy(
        alarmsListState: AlarmsListState = this.alarmsListState,
        calendarViewState: CalendarViewState = this.calendarViewState,
        bottomSheetState: AlarmEditBottomSheetState = this.bottomSheetState,
        deleteDialogState: AlarmDeleteDialogState = this.deleteDialogState,
        snackBarState: SnackBarState = this.snackBarState,
        dayInfoText: String = this.dayInfoText
    ) = AlarmsState(
        alarmsListState,
        calendarViewState,
        bottomSheetState,
        deleteDialogState,
        snackBarState,
        dayInfoText
    )
}

abstract class SnackBarState

class SnackBarOffState : SnackBarState()

data class SnackBarAlarmDeleteState(
    val alarm: AlarmData
) : SnackBarState()

data class SnackBarAlarmCopyState(
    val alarm: AlarmData
) : SnackBarState()

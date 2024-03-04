package com.example.smartalarm.ui.compose.alarms

import com.example.smartalarm.ui.compose.alarms.view.alarmslist.AlarmsListState
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetState
import com.example.smartalarm.ui.compose.alarms.view.calendar.CalendarViewState
import com.example.smartalarm.ui.compose.alarms.view.deletedialog.AlarmDeleteDialogState

data class AlarmsState(
    val alarmsListState: AlarmsListState,
    val calendarViewState: CalendarViewState,
    val bottomSheetState: AlarmEditBottomSheetState,
    val deleteDialogState: AlarmDeleteDialogState,
    val dayInfoText: String
)
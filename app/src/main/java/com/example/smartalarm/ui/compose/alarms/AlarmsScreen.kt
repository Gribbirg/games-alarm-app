package com.example.smartalarm.ui.compose.alarms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.data.data.WeekCalendarData
import com.example.smartalarm.data.repositories.getDefaultWeekDataList
import com.example.smartalarm.data.repositories.getToday
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.AlarmsListLoadingState
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.AlarmsListView
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetOffState
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetView
import com.example.smartalarm.ui.compose.alarms.view.calendar.CalendarView
import com.example.smartalarm.ui.compose.alarms.view.calendar.CalendarViewState
import com.example.smartalarm.ui.compose.alarms.view.calendar.calendarday.CalendarDayState
import com.example.smartalarm.ui.compose.alarms.view.deletedialog.AlarmDeleteDialogOffState
import com.example.smartalarm.ui.compose.alarms.view.deletedialog.AlarmDeleteDialogView
import com.example.smartalarm.ui.compose.view.alarmitem.AlarmItemEvent
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogEvent
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogOffState
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogView
import com.example.smartalarm.ui.theme.GamesAlarmTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    state: AlarmsState,
    onEvent: (AlarmsEvent) -> Unit,
    onAlarmItemEvent: (AlarmItemEvent) -> Unit,
    onTimePickerDialogEvent: (TimePickerDialogEvent) -> Unit,
    navigateToAddAlarmScreen: (Boolean, AlarmData) -> Unit
) {
//    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "РазБудильник") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CalendarView(onEvent, state.calendarViewState)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp, end = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = state.dayInfoText)
                IconButton(onClick = { navigateToAddAlarmScreen(true, AlarmData()) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Добавить будильник")
                }
            }
            AlarmsListView(
                state = state.alarmsListState,
                onAlarmItemEvent = onAlarmItemEvent,
                onEvent = onEvent
            )
        }

        AlarmEditBottomSheetView(
            onEvent = onEvent,
            state = state.bottomSheetState,
            navigateToAddAlarmScreen = navigateToAddAlarmScreen
        )
        AlarmDeleteDialogView(onEvent = onEvent, state = state.deleteDialogState)
        TimePickerDialogView(onEvent = onTimePickerDialogEvent, state = state.timePickerState)


        LaunchedEffect(key1 = state.alarmsSnackBarState) {
            if (state.alarmsSnackBarState is AlarmsSnackBarOffState)
                return@LaunchedEffect
            scope.launch {
                with(state.alarmsSnackBarState) {

                    when (this) {
                        is AlarmsSnackBarAlarmDeleteState -> {
                            val result = snackbarHostState
                                .showSnackbar(
                                    message = "${alarm.name} на ${alarm.getTime()} удалён",
                                    actionLabel = "Отменить",
                                    duration = SnackbarDuration.Short
                                )
                            when (result) {
                                SnackbarResult.ActionPerformed -> {
                                    onEvent(SnackBarAlarmReturnEvent(alarm))
                                }

                                SnackbarResult.Dismissed -> {
                                    onEvent(SnackBarDismissEvent())
                                }
                            }
                        }

                        is AlarmsSnackBarAlarmCopyState -> {
                            val result = snackbarHostState.showSnackbar(
                                message = "${alarm.name} на ${alarm.getTime()} скопирован",
                                duration = SnackbarDuration.Short
                            )

                            if (result == SnackbarResult.Dismissed) {
                                onEvent(SnackBarDismissEvent())
                            }
                        }

                        is AlarmsSnackBarTimeChangeState -> {
                            val result = snackbarHostState.showSnackbar(
                                message = "${alarm.name} переставлен на ${alarm.getTime()}",
                                duration = SnackbarDuration.Short
                            )

                            if (result == SnackbarResult.Dismissed) {
                                onEvent(SnackBarDismissEvent())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE,
    showSystemUi = true,
    apiLevel = 33, name = "main_light",
)
@Composable
fun AlarmsScreenPreview() {
    val weekCalendarData: List<WeekCalendarData> = getDefaultWeekDataList(100)

    fun getDefaultState(): AlarmsState {
        val today = getToday()

        fun getCalendarViewState(selectedDayNum: Int) = CalendarViewState(
            days = weekCalendarData.mapIndexed { weekNum, week ->
                week.daysList.mapIndexed { dayNum, day ->
                    CalendarDayState(
                        day,
                        day.dayNumber,
                        day.toString(),
                        selectedDayNum == weekNum * 7 + dayNum
                    )
                }
            },
            monthTextList = weekCalendarData.map { element -> element.monthList },
            selectedDayNum = selectedDayNum
        )

        return AlarmsState(
            alarmsListState = AlarmsListLoadingState(today.dayOfWeek),
            calendarViewState = getCalendarViewState(today.dayOfWeek),
            bottomSheetState = AlarmEditBottomSheetOffState(),
            dayInfoText = "Будильники на сегодня, 1 января",
            deleteDialogState = AlarmDeleteDialogOffState(),
            alarmsSnackBarState = AlarmsSnackBarOffState(),
            timePickerState = TimePickerDialogOffState()
        )
    }

    GamesAlarmTheme {
        AlarmsScreen(
            getDefaultState(),
            {},
            {},
            {}
        ) { isNew, alarm -> }
    }
}

@Preview(
    showBackground = true,
    wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE,
    showSystemUi = true,
    apiLevel = 33,
)
@Composable
fun AlarmsScreenDarkPreview() {
    val weekCalendarData: List<WeekCalendarData> = getDefaultWeekDataList(100)

    fun getDefaultState(): AlarmsState {
        val today = getToday()

        fun getCalendarViewState(selectedDayNum: Int) = CalendarViewState(
            days = weekCalendarData.mapIndexed { weekNum, week ->
                week.daysList.mapIndexed { dayNum, day ->
                    CalendarDayState(
                        day,
                        day.dayNumber,
                        day.toString(),
                        selectedDayNum == weekNum * 7 + dayNum
                    )
                }
            },
            monthTextList = weekCalendarData.map { element -> element.monthList },
            selectedDayNum = selectedDayNum
        )

        return AlarmsState(
            alarmsListState = AlarmsListLoadingState(today.dayOfWeek),
            calendarViewState = getCalendarViewState(today.dayOfWeek),
            bottomSheetState = AlarmEditBottomSheetOffState(),
            dayInfoText = "Будильники на сегодня, 1 января",
            deleteDialogState = AlarmDeleteDialogOffState(),
            alarmsSnackBarState = AlarmsSnackBarOffState(),
            timePickerState = TimePickerDialogOffState(),
        )
    }

    GamesAlarmTheme(darkTheme = true) {
        AlarmsScreen(
            getDefaultState(),
            {},
            {},
            {}
        ) { isNew, alarm -> }
    }
}
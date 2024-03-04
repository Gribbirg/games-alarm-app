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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.example.smartalarm.data.data.WeekCalendarData
import com.example.smartalarm.data.repositories.getDefaultWeekDataList
import com.example.smartalarm.data.repositories.getToday
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.AlarmsListLoadingState
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.AlarmsListView
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetState
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetView
import com.example.smartalarm.ui.compose.alarms.view.calendar.CalendarView
import com.example.smartalarm.ui.compose.alarms.view.calendar.CalendarViewState
import com.example.smartalarm.ui.compose.alarms.view.calendar.calendarday.CalendarDayState
import com.example.smartalarm.ui.compose.alarms.view.deletedialog.AlarmDeleteDialogState
import com.example.smartalarm.ui.compose.alarms.view.deletedialog.AlarmDeleteDialogView
import com.example.smartalarm.ui.theme.GamesAlarmTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    state: AlarmsState,
    onEvent: (AlarmsEvent) -> Unit,
    onAddAlarmButtonClick: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "РазБудильник") }) },
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
                IconButton(onClick = onAddAlarmButtonClick) {
                    Icon(Icons.Filled.Add, contentDescription = "Добавить будильник")
                }
            }
            AlarmsListView(
                state = state.alarmsListState,
                onEvent = onEvent
            )
        }
        AlarmEditBottomSheetView(onEvent = onEvent, state = state.bottomSheetState)
        AlarmDeleteDialogView(onEvent = onEvent, state = state.deleteDialogState)
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
            bottomSheetState = AlarmEditBottomSheetState(true),
            dayInfoText = "Будильники на сегодня, 1 января",
            deleteDialogState = AlarmDeleteDialogState()
        )
    }

    GamesAlarmTheme {
        AlarmsScreen(
            getDefaultState(),
            {}
        ) {}
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
            bottomSheetState = AlarmEditBottomSheetState(true),
            dayInfoText = "Будильники на сегодня, 1 января",
            deleteDialogState = AlarmDeleteDialogState()
        )
    }

    GamesAlarmTheme(darkTheme = true) {
        AlarmsScreen(
            getDefaultState(),
            {}
        ) {}
    }
}
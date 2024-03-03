package com.example.smartalarm.ui.compose.alarms

import android.util.Log
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.example.smartalarm.data.data.Date
import com.example.smartalarm.data.data.WeekCalendarData
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.AlarmsListListener
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.AlarmsListLoadingState
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.AlarmsListView
import com.example.smartalarm.ui.compose.alarms.view.calendar.CalendarView
import com.example.smartalarm.ui.compose.alarms.view.calendar.CalendarViewState
import com.example.smartalarm.ui.compose.alarms.view.calendar.OnCalendarViewClickListener
import com.example.smartalarm.ui.theme.GamesAlarmTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    state: AlarmsState,
    listener: OnAlarmsScreenClickListener,
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
            CalendarView(listener, state.calendarViewState)
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
                listener = listener
            )
        }
    }
}

interface OnAlarmsScreenClickListener : OnCalendarViewClickListener, AlarmsListListener {

}

@Preview(
    showBackground = true,
    wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE,
    showSystemUi = true,
    apiLevel = 33, name = "main_light",
)
@Composable
fun AlarmsScreenPreview() {
    GamesAlarmTheme {
        AlarmsScreen(
            AlarmsState(
                alarmsListState = AlarmsListLoadingState(0),
                calendarViewState = CalendarViewState(WeekCalendarData.getDefaultList(), 0),
                dayInfoText = "Будильники на сегодня, 1 января"
            ),
            PreviewListener()
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
    GamesAlarmTheme(darkTheme = true) {
        AlarmsScreen(
            AlarmsState(
                alarmsListState = AlarmsListLoadingState(0),
                calendarViewState = CalendarViewState(WeekCalendarData.getDefaultList(), 0),
                dayInfoText = "Будильники на сегодня, 1 января"
            ),
            PreviewListener()
        ) {}
    }
}

class PreviewListener : OnAlarmsScreenClickListener {
    override fun onDayViewClick(dayNum: Int) {}

    override fun pagerScroll(dayNum: Int) {}
}
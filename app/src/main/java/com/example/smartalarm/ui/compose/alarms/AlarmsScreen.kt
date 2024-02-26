package com.example.smartalarm.ui.compose.alarms

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import com.example.smartalarm.data.data.WeekCalendarData
import com.example.smartalarm.ui.compose.alarms.view.calendar.CalendarView
import com.example.smartalarm.ui.compose.alarms.view.calendar.OnCalendarViewClickListener
import com.example.smartalarm.ui.theme.GamesAlarmTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    state: AlarmsState,
    listener: OnAlarmsScreenClickListener
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
            CalendarView(listener, state.weekCalendarData, state.selectedDay)
        }
    }
}

interface OnAlarmsScreenClickListener : OnCalendarViewClickListener {

}

@Preview(
    showBackground = true, wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE, showSystemUi = true,
    apiLevel = 33, name = "main_light",
)
@Composable
fun AlarmsScreenPreview() {
    GamesAlarmTheme {
        AlarmsScreen(
            AlarmsState(WeekCalendarData.getDefaultList(), WeekCalendarData.Date(0, 0, 0)),
            PreviewListener()
        )
    }
}

@Preview(
    showBackground = true, wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE, showSystemUi = true,
    apiLevel = 33,
)
@Composable
fun AlarmsScreenDarkPreview() {
    GamesAlarmTheme(darkTheme = true) {
        AlarmsScreen(
            AlarmsState(WeekCalendarData.getDefaultList(), WeekCalendarData.Date(0, 0, 0)),
            PreviewListener()
        )
    }
}

class PreviewListener : OnAlarmsScreenClickListener {
    override fun onDayViewClick(day: WeekCalendarData.Date) {
        Log.d("Preview", "onDayViewClick: $day")
    }
}
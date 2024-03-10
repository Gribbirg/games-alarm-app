package com.example.smartalarm.ui.compose.addalarm

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.ui.compose.view.alarmitem.AlarmItemEvent
import com.example.smartalarm.ui.compose.view.alarmitem.AlarmItemState
import com.example.smartalarm.ui.compose.view.alarmitem.AlarmsListItemView
import com.example.smartalarm.ui.theme.GamesAlarmTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmScreen(
    onEvent: (AddAlarmEvent) -> Unit,
    onAlarmItemEvent: (AlarmItemEvent) -> Unit,
    state: AddAlarmState
) {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    text = if (state.isNew) "Добавить" else "Изменить"
                )
            }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(10.dp)
        ) {
            AlarmsListItemView(onEvent = onAlarmItemEvent, state = AlarmItemState(state.alarm))
        }
    }
}

@Preview(
    showBackground = true,
    wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE,
    showSystemUi = true,
    apiLevel = 33,
    name = "main_light",
)
@Composable
fun AddAlarmScreenPreview() {
    GamesAlarmTheme {
        AddAlarmScreen(
            onEvent = {},
            state = AddAlarmState(
                isNew = false,
                alarm = AlarmData()
            ),
            onAlarmItemEvent = {}
        )
    }
}
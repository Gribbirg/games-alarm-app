package com.example.smartalarm.ui.compose.view.alarmitem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.VideogameAsset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.ui.theme.GamesAlarmTheme

@Composable
fun AlarmsListItemView(
    onEvent: (AlarmItemEvent) -> Unit,
    state: AlarmItemState
) {
    val isOnState = remember {
        mutableStateOf(state.alarm.isOn)
    }

    LaunchedEffect(key1 = state.alarm.isOn) {
        if (state.alarm.isOn != isOnState.value)
            isOnState.value = state.alarm.isOn
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor =
            if (isOnState.value) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceContainer
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = state.alarm.name)
                IconButton(onClick = { onEvent(AlarmItemChangeEvent(state.alarm)) }) {
                    Icon(Icons.Filled.MoreHoriz, contentDescription = "Изменить")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        onEvent(AlarmItemClockClickedEvent(state.alarm))
                    }
                ) {
                    Text(
                        text = state.alarm.getTime(),
                        fontSize = 50.sp
                    )
                }
                Row {
                    Icon(Icons.Outlined.VideogameAsset, contentDescription = "Игры")
                    Text(text = ": ${state.alarm.gamesList.count { game -> game != 0 }}")
                }
                Switch(
                    checked = isOnState.value,
                    onCheckedChange = { isOn ->
                        isOnState.value = isOn
                        onEvent(AlarmItemSetOnStateEvent(state.alarm, isOn))
                    }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 5.dp, bottom = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Row {

                }
                Row {
                    Icon(
                        Icons.Filled.Vibration,
                        contentDescription = "Вибрация",
                        modifier = Modifier.alpha(if (state.alarm.isVibration) 1f else 0f)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Увеличение громкости",
                        modifier = Modifier.alpha(if (state.alarm.isRisingVolume) 1f else 0f)
                    )
                }
            }
        }
    }
}


@Preview(
    showBackground = true,
    wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE,
    showSystemUi = true,
    apiLevel = 33,
)
@Composable
fun AlarmsListItemViewPreview() {
    GamesAlarmTheme {
        Scaffold {
            Column(
                modifier = Modifier.padding(it)
            ) {
                AlarmsListItemView(
                    {},
                    AlarmItemState(
                        AlarmData(
                            0,
                            "Будильник",
                            8,
                            10
                        )
                    )
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE,
    showSystemUi = true,
    apiLevel = 33,
)
@Composable
fun AlarmsListItemViewDarkPreview() {
    GamesAlarmTheme(darkTheme = true) {
        Scaffold {
            Column(
                modifier = Modifier.padding(it)
            ) {
                AlarmsListItemView(
                    {},
                    AlarmItemState(
                        AlarmData(
                            0,
                            "Будильник",
                            8,
                            10,
                            isVibration = true,
                            isRisingVolume = true
                        )
                    )
                )
            }
        }
    }
}
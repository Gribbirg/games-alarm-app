package com.example.smartalarm.ui.compose.alarms.view.alarmslist.item

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    onEvent: (AlarmsListItemEvent) -> Unit,
    state: AlarmsListItemState
) {
    val isOnState = remember {
        mutableStateOf(state.alarm.isOn)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isOnState.value) MaterialTheme.colorScheme.surfaceContainerHighest
            else MaterialTheme.colorScheme.surfaceContainerLow
        )
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
                IconButton(onClick = { onEvent(AlarmsListItemChangeEvent(state.alarm)) }) {
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
                Text(
                    text = state.alarm.getTime(),
                    fontSize = 50.sp
                )
                Row {
                    Icon(Icons.Outlined.VideogameAsset, contentDescription = "Игры")
                    Text(text = ": ${state.alarm.gamesList.count { game -> game != 0 }}")
                }
                Switch(
                    checked = isOnState.value,
                    onCheckedChange = { isOn ->
                        isOnState.value = isOn
                        onEvent(AlarmsListItemSetOnStateEvent(state.alarm, isOn))
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
                        modifier = Modifier.alpha(if (state.alarm.isVibration) 1f else 0f)
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
                    AlarmsListItemState(
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
                    AlarmsListItemState(
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
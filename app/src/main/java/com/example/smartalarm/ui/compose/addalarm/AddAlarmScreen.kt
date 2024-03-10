package com.example.smartalarm.ui.compose.addalarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.data.repositories.getDayOfWeekShortName
import com.example.smartalarm.ui.compose.view.alarmitem.AlarmItemEvent
import com.example.smartalarm.ui.compose.view.alarmitem.AlarmItemState
import com.example.smartalarm.ui.compose.view.alarmitem.AlarmsListItemView
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogEvent
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogOffState
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogView
import com.example.smartalarm.ui.theme.GamesAlarmTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmScreen(
    onEvent: (AddAlarmEvent) -> Unit,
    onAlarmItemEvent: (AlarmItemEvent) -> Unit,
    onTimePickerDialogEvent: (TimePickerDialogEvent) -> Unit,
    state: AddAlarmState
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
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
                .padding(10.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AlarmsListItemView(onEvent = onAlarmItemEvent, state = AlarmItemState(state.alarm))
            HorizontalDivider(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Время:", fontSize = 25.sp)
                OutlinedButton(onClick = { onEvent(AddAlarmTimeClickedEvent()) }) {
                    Text(text = state.alarm.getTime(), fontSize = 30.sp)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))
            Text(text = "День недели:")
            MultiChoiceSegmentedButtonRow {
                repeat(7) { i ->
                    SegmentedButton(
                        checked = state.daysOfWeek[i],
                        onCheckedChange = { isOn ->
                            onEvent(
                                AddAlarmDayOfWeekSelectedEvent(
                                    i,
                                    isOn
                                )
                            )
                        },
                        shape = getShapeByDayOfWeekNumber(i),
                        icon = {}
                    ) {
                        Text(text = getDayOfWeekShortName(i))
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = state.alarm.name,
                onValueChange = { name -> onEvent(AddAlarmNameChangeEvent(name)) },
                label = { Text(text = "Название") },
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                singleLine = true
            )
            HorizontalDivider(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))
            Text(text = "Мелодия:")
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Название", color = MaterialTheme.colorScheme.primary) // TODO: song
                OutlinedButton(onClick = { /*TODO*/ }) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Изменить")
                }
            }
            HorizontalDivider(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Вибрация:")
                OutlinedIconToggleButton(
                    checked = state.alarm.isVibration,
                    onCheckedChange = { isOn -> onEvent(AddAlarmVibrationChangeEvent(isOn)) },
                    colors = IconButtonDefaults.outlinedIconToggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                {
                    Icon(imageVector = Icons.Filled.Vibration, contentDescription = "Вибрация", tint = MaterialTheme.colorScheme.primary)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))
        }
    }

    TimePickerDialogView(onEvent = onTimePickerDialogEvent, state = state.timePickerDialogState)
}


private fun getShapeByDayOfWeekNumber(dayOfWeek: Int) = when (dayOfWeek) {
    0 -> RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
    6 -> RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
    else -> RoundedCornerShape(0.dp)
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
                alarm = AlarmData(),
                timePickerDialogState = TimePickerDialogOffState(),
                daysOfWeek = MutableList(7) { it == 3 }
            ),
            onAlarmItemEvent = {},
            onTimePickerDialogEvent = {}
        )
    }
}
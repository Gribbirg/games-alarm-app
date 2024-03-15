package com.example.smartalarm.ui.compose.addalarm

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.data.repositories.getDayOfWeekShortName
import com.example.smartalarm.data.utils.RealPathUtil
import com.example.smartalarm.ui.compose.view.alarmitem.AlarmItemEvent
import com.example.smartalarm.ui.compose.view.alarmitem.AlarmItemState
import com.example.smartalarm.ui.compose.view.alarmitem.AlarmsListItemView
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogEvent
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogOffState
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogView
import com.example.smartalarm.ui.theme.GamesAlarmTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmScreen(
    onEvent: (AddAlarmEvent) -> Unit,
    onAlarmItemEvent: (AlarmItemEvent) -> Unit,
    onTimePickerDialogEvent: (TimePickerDialogEvent) -> Unit,
    state: AddAlarmState,
    toAlarmsScreen: (dayOfWeek: Int, isNew: Boolean, alarm: AlarmData) -> Unit,
    toAlarmsScreenBack: () -> Unit,
    toGamesSelectScreen: (AlarmData) -> Unit
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val musicSelectLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        try {
            uri?.let {
                val ringtonePath = RealPathUtil.getRealPath(context, uri).toString()
                Log.i("chosen song", ringtonePath)
                onEvent(AddAlarmRingtoneSelectedEvent(ringtonePath))
            }
        } catch (e: Exception) {
            Log.i("selection fail", e.toString())
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = state.saveFinish) {
        if (state.saveFinish)
            toAlarmsScreen(
                state.daysOfWeek.indexOf(true),
                state.isNew,
                state.alarm
            )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (state.isNew) "Добавить" else "Изменить"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { toAlarmsScreenBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onEvent(AddAlarmPasteEvent())
                        },
                        enabled = state.hasCopiedAlarm
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentPaste,
                            contentDescription = "Вставить"
                        )
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = "Сохранить") },
                icon = { Icon(imageVector = Icons.Filled.Save, contentDescription = "Сохранить") },
                onClick = {
                    onEvent(AddAlarmSaveEvent())
                },
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(10.dp)
                .verticalScroll(rememberScrollState()),
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
                val name = state.alarm.ringtonePath.substringAfterLast("/")
                Text(
                    text = if (name == "") "По умолчанию" else name,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedButton(onClick = { musicSelectLauncher.launch(arrayOf("audio/*")) }) {
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
                    Icon(
                        imageVector = Icons.Filled.Vibration,
                        contentDescription = "Вибрация",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Увеличение громкости:")
                OutlinedIconToggleButton(
                    checked = state.alarm.isRisingVolume,
                    onCheckedChange = { isOn -> onEvent(AddAlarmRisingVolumeChangeEvent(isOn)) },
                    colors = IconButtonDefaults.outlinedIconToggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Вибрация",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))
            Text(text = "Игры:")
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.selectedGamesList.isNotEmpty()) {
                    state.selectedGamesList.forEach { game ->
                        Text(text = game.name)
                    }
                } else {
                    Text(text = "Игр нет!")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = { toGamesSelectScreen(state.alarm) }) {
                    if (state.alarm.gamesList.isEmpty())
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Добавить")
                    else
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Изменить")
                }
            }
            Spacer(modifier = Modifier.height(130.dp))
        }
    }

    TimePickerDialogView(onEvent = onTimePickerDialogEvent, state = state.timePickerDialogState)

    if (state.alertDialogState is AddAlarmAlertDialogDaysNotSelectedState) {
        AlertDialog(
            onDismissRequest = { onEvent(AddAlarmAlertDialogCloseEvent()) },
            confirmButton = {
                TextButton(onClick = { onEvent(AddAlarmAlertDialogCloseEvent()) }) {
                    Text(text = "Понятно")
                }
            },
            title = { Text(text = "Выберите дни") },
            text = { Text(text = "Выберите дни недели, на которые будет установлен ${state.alarm.name}") },
            icon = { Icon(imageVector = Icons.Filled.ErrorOutline, contentDescription = "Ошибка") }
        )
    }

    LaunchedEffect(key1 = state.snackBarState) {
        with(state.snackBarState) {
            when (this) {
                is AddAlarmSnackBarOffState -> return@LaunchedEffect

                is AddAlarmSnackBarPastedState -> {
                    showSimpleSnackBar(
                        onEvent,
                        snackbarHostState,
                        "${alarm.name} на ${alarm.getTime()} вставлен"
                    )
                }

                is AddAlarmSnackBarNothingToPastedState -> {
                    showSimpleSnackBar(
                        onEvent,
                        snackbarHostState,
                        "Нет скопированного будильника"
                    )
                }
            }
        }
    }
}


private fun getShapeByDayOfWeekNumber(dayOfWeek: Int) = when (dayOfWeek) {
    0 -> RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
    6 -> RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
    else -> RoundedCornerShape(0.dp)
}

private suspend fun showSimpleSnackBar(
    onEvent: (AddAlarmSnackBarClosedEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    text: String
) = withContext(Dispatchers.IO) {
    val result = snackbarHostState.showSnackbar(
        message = text,
        duration = SnackbarDuration.Short
    )

    if (result == SnackbarResult.Dismissed) {
        onEvent(AddAlarmSnackBarClosedEvent())
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
                alarm = AlarmData(),
                timePickerDialogState = TimePickerDialogOffState(),
                daysOfWeek = MutableList(7) { it == 3 },
                alertDialogState = AddAlarmAlertDialogOffState(),
                snackBarState = AddAlarmSnackBarOffState(),
                selectedGamesList = listOf()
            ),
            onAlarmItemEvent = {},
            onTimePickerDialogEvent = {},
            toAlarmsScreen = { _, _, _ -> },
            toAlarmsScreenBack = {},
            toGamesSelectScreen = {}
        )
    }
}
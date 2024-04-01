package com.example.smartalarm.ui.compose.view.timepickerdialog

import android.icu.text.CaseMap.Title
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogView(
    onEvent: (TimePickerDialogEvent) -> Unit,
    state: TimePickerDialogState
) {
    if (state is TimePickerDialogOnState) {
        val timePickerState = rememberTimePickerState(
            state.alarm.timeHour,
            state.alarm.timeMinute,
            true
        )

        Dialog(onDismissRequest = { onEvent(TimePickerDialogDismissEvent()) }) {
            Card(
                modifier = Modifier
                    .padding(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
                        .fillMaxWidth(),
//                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Выберите время")
                    Spacer(modifier = Modifier.height(10.dp))
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Absolute.Right
                    ) {
                        TextButton(onClick = { onEvent(TimePickerDialogDismissEvent()) }) {
                            Text(text = "Отменить")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(onClick = {
                            onEvent(
                                TimePickerDialogSetEvent(
                                    timePickerState.hour,
                                    timePickerState.minute,
                                    state.alarm
                                )
                            )
                        }) {
                            Text(text = "Сохранить")
                        }
                    }
                }
            }
        }
    }
}
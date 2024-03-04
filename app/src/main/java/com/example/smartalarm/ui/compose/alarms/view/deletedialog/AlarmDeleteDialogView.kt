package com.example.smartalarm.ui.compose.alarms.view.deletedialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun AlarmDeleteDialogView(
    onEvent: (AlarmDeleteDialogEvent) -> Unit,
    state: AlarmDeleteDialogState
) {
    if (state.alarm != null) {
        AlertDialog(
            icon = { Icon(Icons.Filled.AlarmOff, contentDescription = "Удалить будильник") },
            title = { Text(text = "Удалить \"${state.alarm.name}\"?") },
            text = { Text(text = "Вы уверены, что хотите удалить \"${state.alarm.name}\"? Восстановление невозможно.") },
            onDismissRequest = { onEvent(AlarmDeleteDialogDismissEvent()) },
            confirmButton = {
                TextButton(onClick = { onEvent(AlarmDeleteDialogConfirmEvent(state.alarm)) }) {
                    Text(text = "Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(AlarmDeleteDialogDismissEvent()) }) {
                    Text(text = "Отменить")
                }
            })
    }
}
package com.example.smartalarm.ui.compose.alarms.view.bottomsheet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.smartalarm.App
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.ui.theme.GamesAlarmTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditBottomSheetView(
    onEvent: (AlarmEditBottomSheetEvent) -> Unit,
    state: AlarmEditBottomSheetState
) {
    val bottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    if (state is AlarmEditBottomSheetOnState) {
        ModalBottomSheet(
            onDismissRequest = {
                onEvent(AlarmEditBottomSheetCloseEvent())
            },
            sheetState = bottomSheetState,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(text = "Будильник \"${state.alarm.name}\" на ${state.alarm.getTime()}")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FilledTonalIconButton(
                        onClick = { onEvent(AlarmEditBottomSheetOnDeleteClickedEvent(state.alarm)) }
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Удалить"
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(text = "Удалить")
                }
                Column(
                    modifier = Modifier
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FilledTonalIconButton(
                        onClick = { scope.launch {
                            copyAlarm(context, state.alarm)
                            onEvent(AlarmEditBottomSheetOnCopyClickedEvent(state.alarm))
                        } }
                    ) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = "Скопировать"
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(text = "Скопировать")
                }
                Column(
                    modifier = Modifier
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FilledTonalIconButton(
                        onClick = { onEvent(AlarmEditBottomSheetOnEditClickedEvent(state.alarm)) }
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Изменить"
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(text = "Изменить")
                }
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

private suspend fun copyAlarm(context: Context, alarm: AlarmData) = withContext(Dispatchers.IO) {
    val clipboard =
        context.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val intent = Intent(context.applicationContext, App::class.java)
    intent.putExtra("alarm simple", AlarmSimpleData(alarm).toStringArray())
    intent.putExtra("alarm games", alarm.gamesList)
    val clip = ClipData.newIntent("alarm copy", intent)
    clipboard.setPrimaryClip(clip)
}

@Preview(
    showBackground = true,
    wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE,
    showSystemUi = true,
    apiLevel = 33,
)
@Composable
fun AlarmEditBottomSheetViewPreview() {
    GamesAlarmTheme {
        Scaffold {
            Box(
                modifier = Modifier
                    .padding(it)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {}
            AlarmEditBottomSheetView(
                {},
                AlarmEditBottomSheetOnState(AlarmData())
            )
        }
    }
}
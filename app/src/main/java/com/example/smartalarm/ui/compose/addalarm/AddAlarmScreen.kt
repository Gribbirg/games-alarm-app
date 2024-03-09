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
import com.example.smartalarm.ui.theme.GamesAlarmTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmScreen(
    onEvent: (AddAlarmEvent) -> Unit,
    state: AddAlarmState
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Добавить") })
        }
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {

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
        AddAlarmScreen(onEvent = {}, state = AddAlarmState())
    }
}
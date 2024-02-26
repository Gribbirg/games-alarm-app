package com.example.smartalarm.ui.compose.alarms

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.Wallpapers
import com.example.smartalarm.ui.theme.GamesAlarmTheme
import com.google.android.material.color.MaterialColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    state: AlarmsState,

    ) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "РазБудильник") }) },
    ) {
        Column (
            modifier = Modifier
                .padding(it)
                .fillMaxWidth()
                .fillMaxHeight()
            ,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "test")
        }
    }
}

@Preview(
    showBackground = true, wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE, showSystemUi = true,
    apiLevel = 33,
)
@Composable
fun AlarmsScreenPreview() {
    GamesAlarmTheme {
        AlarmsScreen(state = AlarmsState())
    }
}

@Preview(
    showBackground = true, wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE, showSystemUi = true,
    apiLevel = 33,
)
@Composable
fun AlarmsScreenDarkPreview() {
    GamesAlarmTheme (darkTheme = true) {
        AlarmsScreen(state = AlarmsState())
    }
}
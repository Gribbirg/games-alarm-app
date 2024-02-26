package com.example.smartalarm.ui.compose.alarms

import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.google.android.material.color.MaterialColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    state: AlarmsState,

    ) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "РазБудильник") }) },
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
        Text(text = it.toString())
    }
}
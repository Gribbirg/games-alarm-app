package com.example.smartalarm.ui.compose.alarms.view.bottomsheet

import com.example.smartalarm.data.data.AlarmData

data class AlarmEditBottomSheetState(
    val state: Boolean,
    val alarm: AlarmData? = null
)
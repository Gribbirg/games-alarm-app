package com.example.smartalarm.ui.compose.addalarm

import com.example.smartalarm.data.data.AlarmData

class AddAlarmState(
    val alarm: AlarmData = AlarmData(),
    val isNew: Boolean
) {}
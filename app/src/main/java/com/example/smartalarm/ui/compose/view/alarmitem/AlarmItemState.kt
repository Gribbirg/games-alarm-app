package com.example.smartalarm.ui.compose.view.alarmitem

import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.data.data.GameData

data class AlarmItemState(
    val alarm: AlarmData,
    val gamesDataList: List<GameData>
)
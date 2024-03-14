package com.example.smartalarm.ui.compose.gameselect.gameitem

import com.example.smartalarm.data.data.GameData

data class GameItemState(
    val game: GameData,
    val isOn: Boolean = false,
    val level: Int = 0,

    val isExpanded: Boolean = false,
    val isMenuOpened: Boolean = false
)
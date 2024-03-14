package com.example.smartalarm.ui.compose.gameselect.gameitem

import com.example.smartalarm.ui.compose.gameselect.GameSelectEvent

abstract class GameItemEvent(
    val id: Int
) : GameSelectEvent()

class GameItemExpandedEvent(id: Int) : GameItemEvent(id)

abstract class GameItemLevelSelectMenuEvent(id: Int) : GameItemEvent(id)

class GameItemLevelSelectMenuStateChangeEvent(
    id: Int,
    val isOpen: Boolean,
) : GameItemLevelSelectMenuEvent(id)

class GameItemLevelSelectMenuSelectEvent(
    id: Int,
    val levelId: Int
) : GameItemLevelSelectMenuEvent(id)
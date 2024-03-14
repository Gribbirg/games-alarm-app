package com.example.smartalarm.ui.compose.gameselect

import com.example.smartalarm.data.data.GameData
import com.example.smartalarm.ui.compose.gameselect.gameitem.GameItemState

abstract class GameSelectState

class GameSelectLoadingState : GameSelectState()

data class GameSelectLoadedState(
    val gamesList: List<GameItemState>
) : GameSelectState()


class GameSelectErrorState : GameSelectState()
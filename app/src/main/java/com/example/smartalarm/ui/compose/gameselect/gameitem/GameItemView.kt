package com.example.smartalarm.ui.compose.gameselect.gameitem

import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun GameItemView(
    onEvent: (GameItemEvent) -> Unit,
    state: GameItemState
) {
    Card {
        Text(text = state.game.name)
    }
}
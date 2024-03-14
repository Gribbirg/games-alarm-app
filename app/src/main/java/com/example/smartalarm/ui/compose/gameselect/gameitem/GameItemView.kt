package com.example.smartalarm.ui.compose.gameselect.gameitem

import android.util.Log
import android.widget.ToggleButton
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartalarm.data.data.GameData
import com.example.smartalarm.ui.theme.GamesAlarmTheme

const val EXPAND_ANIMATION_DURATION = 300

@Composable
fun GameItemView(
    onEvent: (GameItemEvent) -> Unit,
    state: GameItemState
) {

    val transitionState = remember {
        MutableTransitionState(state.isExpanded).apply {
            targetState = !state.isExpanded
        }
    }
    val transition = updateTransition(targetState = transitionState, label = "")
    val arrowRotationDegree by transition.animateFloat({
        tween(durationMillis = EXPAND_ANIMATION_DURATION)
    }, label = "") {
        Log.d("animation", "GameItemView: Arrow state: ${it.currentState}")
        if (state.isExpanded) 0f else 180f
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable { onEvent(GameItemExpandedEvent(state.game.id)) },
        colors = CardDefaults.cardColors(
            containerColor =
            if (state.isOn) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceContainer
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = state.game.name, fontSize = 20.sp)
            Row {
                OutlinedButton(onClick = { onEvent(GameItemLevelSelectMenuStateChangeEvent(state.game.id, true)) }) {
                    Text(text = state.game.levels[state.level].name, color = MaterialTheme.colorScheme.secondary)
                    DropdownMenu(
                        expanded = state.isMenuOpened,
                        onDismissRequest = { onEvent(GameItemLevelSelectMenuStateChangeEvent(state.game.id, false)) }
                    ) {
                        state.game.levels.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(text = level.name) },
                                onClick = { onEvent(GameItemLevelSelectMenuSelectEvent(state.game.id, level.id)) })
                        }
                    }
                }

                Checkbox(checked = state.isOn, onCheckedChange = { TODO() })
            }
        }

        AnimatedVisibility(visible = state.isExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = state.game.description)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                modifier = Modifier
                    .rotate(arrowRotationDegree),
                onClick = { onEvent(GameItemExpandedEvent(state.game.id)) }
            ) {
                Icon(imageVector = Icons.Filled.KeyboardArrowDown, contentDescription = "Расширить")
            }
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
fun GameItemViewPreview() {
    GamesAlarmTheme {
        GameItemView(
            onEvent = {},
            state = GameItemState(
                GameData(
                    1,
                    "Арифметика",
                    "Бла бла бла",
                    listOf(GameData.GameLevelData(0, "Bla", "bla bla bla"))
                )
            )
        )
    }
}
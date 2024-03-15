package com.example.smartalarm.ui.compose.gameselect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartalarm.data.data.GameData
import com.example.smartalarm.ui.compose.gameselect.gameitem.GameItemView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSelectScreen(
    onEvent: (GameSelectEvent) -> Unit,
    state: GameSelectState,
    onNavBack: () -> Unit,
    onSave: (gamesList: List<GameData>) -> Unit
) {
    LaunchedEffect(key1 = state) {
        when (state) {
            is GameSelectLoadingState -> {
                onEvent(GameSelectLoadEvent())
            }

            is GameSelectSaveAndExitState -> {
                onSave(state.gamesList)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Выбрать игры") },
                navigationIcon = {
                    IconButton(onClick = { onNavBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            if (state is GameSelectLoadedState) {
                ExtendedFloatingActionButton(
                    text = { Text(text = "Сохранить") },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Сохранить"
                        )
                    },
                    onClick = {
                        onEvent(GameSelectSaveAndExitEvent())
                    },
                )
            }
        }
    ) { paddingValue ->
        when (state) {
            is GameSelectLoadedState -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(paddingValue),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    state.gamesList.forEach {
                        GameItemView(onEvent = onEvent, state = it)
                    }
                    Spacer(modifier = Modifier.height(130.dp))
                }
            }

            is GameSelectLoadingState -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(paddingValue),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            is GameSelectErrorState -> {
                Text(text = state.text, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
package com.example.smartalarm.ui.compose.gameselect

import android.app.Application
import androidx.compose.runtime.internal.updateLiveLiteralValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.data.data.AlarmGameData
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.repositories.AlarmDbRepository
import com.example.smartalarm.data.repositories.GamesListRepository
import com.example.smartalarm.ui.compose.gameselect.gameitem.GameItemEvent
import com.example.smartalarm.ui.compose.gameselect.gameitem.GameItemExpandedEvent
import com.example.smartalarm.ui.compose.gameselect.gameitem.GameItemLevelSelectMenuEvent
import com.example.smartalarm.ui.compose.gameselect.gameitem.GameItemLevelSelectMenuSelectEvent
import com.example.smartalarm.ui.compose.gameselect.gameitem.GameItemLevelSelectMenuStateChangeEvent
import com.example.smartalarm.ui.compose.gameselect.gameitem.GameItemState
import com.example.smartalarm.ui.compose.gameselect.gameitem.GameItemStateChangeEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameSelectViewModel(application: Application) : AndroidViewModel(application) {

    private val gamesListRepository = GamesListRepository(application.applicationContext)

    var gamesRecycler: MutableLiveData<ArrayList<AlarmGameData>> = MutableLiveData()
    var games: ArrayList<AlarmGameData> = ArrayList()
    private val alarmDbRepository = AlarmDbRepository(
        AlarmsDB.getInstance(getApplication())?.alarmsDao()!!
    )

    private val _state = MutableStateFlow(getDefaultState())
    val state = _state.asStateFlow()

    fun onEvent(event: GameSelectEvent) {
        when (event) {
            is GameSelectLoadEvent -> {
                viewModelScope.launch {
                    loadGames()
                }
            }

            is GameSelectSaveAndExitEvent -> {
                _state.update { state ->
                    if (state is GameSelectLoadedState) {
                        GameSelectSaveAndExitState(
                            state.gamesList,
                            state.gamesList.filter { it.isOn }.map { it.game }
                        )
                    } else {
                        state
                    }
                }
            }

            is GameItemEvent -> onGameItemEvent(event)
        }
    }

    private fun getDefaultState(): GameSelectState = GameSelectLoadingState()

    private fun onGameItemEvent(event: GameItemEvent) {
        when (event) {
            is GameItemExpandedEvent -> updateGamesListItemState(event.id) { it.copy(isExpanded = !it.isExpanded) }

            is GameItemLevelSelectMenuEvent -> onGameItemLevelSelectMenuEvent(event)

            is GameItemStateChangeEvent -> updateGamesListItemState(event.id) { it.copy(isOn = event.isOn) }
        }
    }

    private fun onGameItemLevelSelectMenuEvent(event: GameItemLevelSelectMenuEvent) {
        when (event) {
            is GameItemLevelSelectMenuStateChangeEvent -> updateGamesListItemState(event.id) {
                it.copy(
                    isMenuOpened = event.isOpen
                )
            }

            is GameItemLevelSelectMenuSelectEvent -> updateGamesListItemState(event.id) {
                it.copy(
                    level = event.levelId,
                    isMenuOpened = false
                )
            }
        }
    }

    private fun updateGamesListItemState(id: Int, action: (GameItemState) -> GameItemState) {
        _state.update { state ->
            if (state is GameSelectLoadedState)
                state.copy(
                    gamesList = List(state.gamesList.size) { i ->
                        state.gamesList[i].let {
                            if (it.game.id == id)
                                action(it)
                            else
                                it
                        }
                    }
                )
            else
                state
        }
    }

    private suspend fun loadGames() = withContext(Dispatchers.IO) {
        val games = getGames()
        _state.update {
            games?.let {
                GameSelectLoadedState(it)
            } ?: GameSelectErrorState("Не удалось загрузить игры")
        }
    }

    private suspend fun getGames(): List<GameItemState>? = withContext(Dispatchers.IO) {
        val games = gamesListRepository.getList()
        return@withContext games?.size?.let { List(it) { i -> GameItemState(games[i]) } }
    }

    fun setGames(gamesIndexesList: List<Int>) {
        when (val currentState = state.value) {
            is GameSelectLoadedState -> {
                _state.update {
                    currentState.copy(
                        gamesList = List(currentState.gamesList.size) { i ->
                            currentState.gamesList[i].copy(
                                isOn = gamesIndexesList.contains(currentState.gamesList[i].game.id)
                            )
                        }
                    )
                }
            }

            is GameSelectSaveAndExitState -> {
                _state.update {
                    GameSelectLoadedState(
                        gamesList = List(currentState.gamesStatesList.size) { i ->
                            currentState.gamesStatesList[i].copy(
                                isOn = gamesIndexesList.contains(currentState.gamesStatesList[i].game.id)
                            )
                        }
                    )
                }
            }

            else -> {
                viewModelScope.launch {
                    loadGames()
                    _state.update { state ->
                        if (state is GameSelectLoadedState) {
                            state.copy(
                                gamesList = List(state.gamesList.size) { i ->
                                    state.gamesList[i].copy(
                                        isOn = gamesIndexesList.contains(state.gamesList[i].game.id)
                                    )
                                }
                            )
                        } else {
                            state
                        }
                    }
                }
            }
        }
    }

    fun getDifficultiesList(): ArrayList<Int> {
        val res: ArrayList<Int> = ArrayList()
        for (game in games) {
            res.add(if (game.isOn) game.difficulty else 0)
        }
        return res
    }
}
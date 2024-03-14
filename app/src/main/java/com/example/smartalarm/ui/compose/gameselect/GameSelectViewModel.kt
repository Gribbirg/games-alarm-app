package com.example.smartalarm.ui.compose.gameselect

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.data.data.AlarmGameData
import com.example.smartalarm.data.constants.ALL_GAMES
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.repositories.AlarmDbRepository
import com.example.smartalarm.data.repositories.GamesListRepository
import com.example.smartalarm.ui.compose.gameselect.gameitem.GameItemState
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
                    val games = getGames()
                    _state.update {
                        games?.let {
                            GameSelectLoadedState(it)
                        } ?: GameSelectErrorState()
                    }
                }
            }
        }
    }

    private fun getDefaultState(): GameSelectState = GameSelectLoadingState()

    private suspend fun getGames(): List<GameItemState>? = withContext(Dispatchers.IO) {
        val games = gamesListRepository.getList()
        return@withContext games?.size?.let { List(it) { i -> GameItemState(games[i]) } }
    }

    fun getDifficultiesList(): ArrayList<Int> {
        val res: ArrayList<Int> = ArrayList()
        for (game in games) {
            res.add(if (game.isOn) game.difficulty else 0)
        }
        return res
    }
}
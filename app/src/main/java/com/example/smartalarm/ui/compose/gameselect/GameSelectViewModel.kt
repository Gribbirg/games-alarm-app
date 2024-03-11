package com.example.smartalarm.ui.compose.gameselect

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.data.data.AlarmGameData
import com.example.smartalarm.data.constants.ALL_GAMES
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.repositories.AlarmDbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameSelectViewModel(application: Application) : AndroidViewModel(application) {
    var gamesRecycler: MutableLiveData<ArrayList<AlarmGameData>> = MutableLiveData()
    var games: ArrayList<AlarmGameData> = ArrayList()
    private val alarmDbRepository = AlarmDbRepository(
        AlarmsDB.getInstance(getApplication())?.alarmsDao()!!
    )

    private val _state = MutableStateFlow(getDefaultState())
    val state = _state.asStateFlow()

    fun onEvent(event: GameSelectEvent) {

    }

    private fun getDefaultState() = GameSelectState()

    fun getGames(gamesDifficulties: ArrayList<Int>?) {
        viewModelScope.launch {
            val gamesDataList = alarmDbRepository.getGames()
            games = ArrayList()
            for (i in ALL_GAMES.indices) {
                games.add(AlarmGameData(gamesDataList[i], gamesDifficulties?.get(i) ?: 0))
            }
            gamesRecycler.postValue(games)
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
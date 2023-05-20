package com.example.smartalarm.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.data.data.AlarmGameData
import com.example.smartalarm.data.db.ALL_GAMES
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.repositories.AlarmDbRepository
import kotlinx.coroutines.launch

class GameChoiceViewModel(application: Application) : AndroidViewModel(application) {
    var gamesRecycler: MutableLiveData<ArrayList<AlarmGameData>> = MutableLiveData()
    var games: ArrayList<AlarmGameData> = ArrayList()
    private val alarmDbRepository = AlarmDbRepository(
        AlarmsDB.getInstance(getApplication())?.alarmsDao()!!
    )

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
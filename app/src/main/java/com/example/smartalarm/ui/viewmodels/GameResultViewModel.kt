package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.db.RecordsData
import com.example.smartalarm.data.repositories.AlarmDbRepository
import com.example.smartalarm.data.repositories.getTodayDate
import kotlinx.coroutines.launch

class GameResultViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmDbRepository = AlarmDbRepository(
        AlarmsDB.getInstance(getApplication())?.alarmsDao()!!
    )

    fun setGameResult(alarmId: Long, gameId: Int, score: Int, time: String) {
        viewModelScope.launch {
            val alarm = alarmDbRepository.getAlarmWithGames(alarmId)
            val game = alarmDbRepository.getGameById(gameId)
            alarmDbRepository.insertRecord(
                RecordsData(
                    gameId = gameId,
                    gameName = game.name,
                    recordScore = score,
                    recordTime = time,
                    date = getTodayDate()
                ),
                alarm
            )
        }
    }

}
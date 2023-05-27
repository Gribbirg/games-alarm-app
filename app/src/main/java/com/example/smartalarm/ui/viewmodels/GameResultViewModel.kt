package com.example.smartalarm.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.data.data.AccountData
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.db.RecordsData
import com.example.smartalarm.data.repositories.AlarmCreateRepository
import com.example.smartalarm.data.repositories.AlarmDbRepository
import com.example.smartalarm.data.repositories.AuthRepository
import com.example.smartalarm.data.repositories.getTodayDate
import kotlinx.coroutines.launch

class GameResultViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmDbRepository = AlarmDbRepository(
        AlarmsDB.getInstance(getApplication())?.alarmsDao()!!
    )

    private val alarmCreateRepository = AlarmCreateRepository(application.applicationContext)
    private val authRepository = AuthRepository

    val currentUser: MutableLiveData<AccountData?> = MutableLiveData()

    init {
        authRepository.currentAccount.observeForever {
            currentUser.postValue(if (it != null) AccountData(it) else null)
        }
    }

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
            if (alarm.alarmSimpleData.activateDate == null) {
                alarmCreateRepository.create(alarm)
            } else {
                alarmDbRepository.deleteAlarmFromDb(alarm.alarmSimpleData)
            }
        }
    }
}
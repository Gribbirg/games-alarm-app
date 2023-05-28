package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.data.data.AccountData
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.db.RecordsData
import com.example.smartalarm.data.repositories.AlarmCreateRepository
import com.example.smartalarm.data.repositories.AlarmDbRepository
import com.example.smartalarm.data.repositories.AuthRepository
import com.example.smartalarm.data.repositories.getCurrentDateString
import com.example.smartalarm.data.repositories.getCurrentTimeString
import com.example.smartalarm.data.repositories.getTodayDate
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class GameResultViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmDbRepository = AlarmDbRepository(
        AlarmsDB.getInstance(getApplication())?.alarmsDao()!!
    )

    private val alarmCreateRepository = AlarmCreateRepository(application.applicationContext)
    private val authRepository = AuthRepository

    val currentUser: MutableLiveData<String> = MutableLiveData()

    val currentTime: MutableLiveData<String> = MutableLiveData()

    init {
        authRepository.currentAccount.observeForever {
            currentUser.postValue(if (it != null) "Доброе утро,\n${AccountData(it).name}!" else "Доброе утро!")
        }

        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                currentTime.postValue(getCurrentTimeString())
            }
        }, 0, 1000)
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

    fun getCurrentDate(): String {
        return getCurrentDateString()
    }
}
package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.data.data.AccountData
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.repositories.AlarmCreateRepository
import com.example.smartalarm.data.repositories.AlarmDbRepository
import com.example.smartalarm.data.repositories.AuthRepository
import com.example.smartalarm.data.repositories.UsersRealtimeDatabaseRepository
import com.example.smartalarm.data.repositories.isAhead
import kotlinx.coroutines.launch

class SettingsFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository
    private val usersRealtimeDatabaseRepository = UsersRealtimeDatabaseRepository
    private val alarmCreateRepository = AlarmCreateRepository(application.applicationContext)
    var currentUser: AccountData? = null

    private val alarmDbRepository = AlarmDbRepository(
        AlarmsDB.getInstance(getApplication())?.alarmsDao()!!
    )

    val loadResult: MutableLiveData<Boolean?> = MutableLiveData()

    init {
        authRepository.currentAccount.observeForever {
            currentUser = if (it != null) AccountData(it) else null
        }
    }

    fun loadAlarmsFromInternet(): Boolean {
        if (currentUser != null) {
            viewModelScope.launch {
                val alarmList = MutableLiveData<List<AlarmData>>()

                alarmList.observeForever {
                    viewModelScope.launch {
                        val alarms = alarmDbRepository.getAllAlarms()
                        for (alarm in alarms)
                            alarmCreateRepository.cancel(alarm)
                        alarmDbRepository.deleteAllAlarms()

                        for (alarm in it) {
                            if (alarm.activateDate != null)
                                if (!isAhead(
                                        alarm.activateDate!!,
                                        alarm.timeHour,
                                        alarm.timeMinute
                                    )
                                )
                                    continue
                            alarmDbRepository.insertAlarmToDb(alarm)
                            alarmCreateRepository.create(alarm)
                        }
                    }
                }

                usersRealtimeDatabaseRepository.getAlarms(currentUser!!, alarmList, loadResult)
            }
            return true
        }
        return false
    }

    fun loadAlarmsOfCurrentUser(): Boolean {
        if (currentUser != null) {
            viewModelScope.launch {
                usersRealtimeDatabaseRepository.addAlarmsToUser(
                    currentUser!!,
                    alarmDbRepository.getAllAlarms(),
                    loadResult
                )
            }
            return true
        }
        return false
    }

    fun resetLoadResult() {
        loadResult.postValue(null)
    }

    fun deleteAlarmsFromCloud(): Boolean {
        if (currentUser != null) {
            viewModelScope.launch {
                usersRealtimeDatabaseRepository.deleteAlarmsOfUser(currentUser!!, loadResult)
            }
            return true
        }
        return false
    }

    fun deleteRecordsFromCloud(): Boolean {
        if (currentUser != null) {
            viewModelScope.launch {
                usersRealtimeDatabaseRepository.deleteRecordsOfUser(currentUser!!, loadResult)
            }
            return true
        }
        return false
    }

    fun deleteAccountCloud(): Boolean {
        if (currentUser != null) {
            viewModelScope.launch {
                val localResult = MutableLiveData<Boolean?>()

                localResult.observeForever {
                    viewModelScope.launch {
                        if (it == true) {
                            usersRealtimeDatabaseRepository.deleteAccount(currentUser!!, loadResult)
                            authRepository.singOut()
                        } else
                            localResult.postValue(false)
                    }
                }
                usersRealtimeDatabaseRepository.deleteRecordsOfUser(currentUser!!, localResult)
            }
            return true
        }
        return false
    }

    fun exitFromAccount(): Boolean {
        if (currentUser != null) {
            viewModelScope.launch {
                authRepository.singOut()
                loadResult.postValue(true)
            }
            return true
        }
        return false
    }

    fun deleteAllRecordsFromDb() {
        viewModelScope.launch {
            alarmDbRepository.deleteAllRecords()
            loadResult.postValue(true)
        }
    }

    fun deleteAllAlarmsFromDb() {
        viewModelScope.launch {
            alarmDbRepository.deleteAllAlarms()
            loadResult.postValue(true)
        }
    }
}
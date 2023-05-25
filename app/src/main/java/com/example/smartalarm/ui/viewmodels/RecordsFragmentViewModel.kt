package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.data.data.AccountData
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.db.GameData
import com.example.smartalarm.data.db.getRecordsList
import com.example.smartalarm.data.repositories.AlarmDbRepository
import com.example.smartalarm.data.repositories.AuthRepository
import com.example.smartalarm.data.repositories.UsersRealtimeDatabaseRepository
import kotlinx.coroutines.launch

class RecordsFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val alarmDbRepository = AlarmDbRepository(
        AlarmsDB.getInstance(getApplication())?.alarmsDao()!!
    )

    private val authRepository = AuthRepository
    private val usersRealtimeDatabaseRepository = UsersRealtimeDatabaseRepository
    var currentUser: AccountData? = null
    val myRecordsData: MutableLiveData<ArrayList<GameData>> = MutableLiveData()
    val allRecordsData: MutableLiveData<List<AccountData>> = MutableLiveData()

    init {
        authRepository.currentAccount.observeForever {
            currentUser = if (it != null) AccountData(it) else null
        }
    }


    fun getRecordsFromDb(state: Int) {
        viewModelScope.launch {
            when (state) {
                0 -> myRecordsData.postValue(ArrayList(alarmDbRepository.getGames()))
                1 -> {
                    val fromDb = alarmDbRepository.getRecordsByDate()
                    val res: ArrayList<GameData> = ArrayList()
                    for (record in fromDb)
                        res.add(GameData(record))
                    myRecordsData.postValue(res)
                }

                2 -> {
                    if (currentUser != null) {
                        usersRealtimeDatabaseRepository.getTopRecords(allRecordsData)
                    } else {
                        allRecordsData.postValue(listOf())
                    }
                }

                3 -> {
                    if (currentUser != null) {
                        val users = MutableLiveData<List<AccountData>>()
                        usersRealtimeDatabaseRepository.getAllUsers(users)
                        users.observeForever {
                            val records = mutableListOf<AccountData>()
                            for (user in it) {
                                for (record in getRecordsList(user.records!!)) {

                                    if (record != null) {

                                        records.add(
                                            AccountData(
                                                user.uid,
                                                user.email,
                                                user.name,
                                                user.photo,
                                                record.toString()
                                            )
                                        )

                                    }
                                }
                            }
                            records.sortBy { -it.records!!.split(';')[2].toInt() }
                            allRecordsData.postValue(records)
                        }
                    } else {
                        allRecordsData.postValue(listOf())
                    }
                }
            }
        }
    }

    fun shareRecord(gameData: GameData): Boolean {
        if (currentUser == null) return false
        viewModelScope.launch {
            usersRealtimeDatabaseRepository.updateUserRecords(currentUser!!, gameData)
        }
        return true
    }
}
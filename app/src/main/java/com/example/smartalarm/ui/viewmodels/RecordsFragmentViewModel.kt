package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.data.data.AccountData
import com.example.smartalarm.data.data.RecordInternetData
import com.example.smartalarm.data.data.getRecordsList
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.db.RecordsData
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
    val myRecordsData: MutableLiveData<ArrayList<RecordsData>> = MutableLiveData()
    val allRecordsData: MutableLiveData<List<AccountData>> = MutableLiveData()
    val shareResult: MutableLiveData<Boolean?> = MutableLiveData()

    init {
        authRepository.currentAccount.observeForever {
            currentUser = if (it != null) AccountData(it) else null
        }
    }


    fun getRecordsFromDb(state: Int) {
        viewModelScope.launch {
            when (state) {
                0 -> myRecordsData.postValue(alarmDbRepository.getTopRecords())
                1 -> {
                    myRecordsData.postValue(ArrayList(alarmDbRepository.getRecordsByScore()))
                }

                2 -> {
                    usersRealtimeDatabaseRepository.getTopRecords(allRecordsData)
                }

                3 -> {
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
                        records.sortBy {
                            -it.records!!.split(';')[3].toInt()
                        }
                        allRecordsData.postValue(records)
                    }
                }
            }
        }
    }

    fun shareRecord(recordsData: RecordsData, state: Int): Boolean {
        if (currentUser == null) return false
        viewModelScope.launch {
            usersRealtimeDatabaseRepository.updateUserRecords(
                currentUser!!,
                RecordInternetData(recordsData),
                shareResult
            )
            alarmDbRepository.updateRecord(recordsData)
            getRecordsFromDb(state)
        }
        return true
    }

    fun resetShareResult() {
        shareResult.postValue(null)
    }
}
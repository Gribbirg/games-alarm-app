package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.data.data.AccountData
import com.example.smartalarm.data.data.getRecordsList
import com.example.smartalarm.data.repositories.UsersRealtimeDatabaseRepository
import kotlinx.coroutines.launch

class ProfileOtherViewModel(application: Application) : AndroidViewModel(application) {

    private val usersRealtimeDatabaseRepository = UsersRealtimeDatabaseRepository

    val user: MutableLiveData<AccountData> = MutableLiveData()
    val userRecords: MutableLiveData<List<AccountData>> = MutableLiveData()

    fun getUser(userId: String) {
        viewModelScope.launch {
            usersRealtimeDatabaseRepository.getUser(userId, user)
        }
    }

    fun getRecords() {
        viewModelScope.launch {
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
                userRecords.postValue(records)
            }
        }
    }
}
package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.data.data.AccountData
import com.example.smartalarm.data.data.RecordInternetData
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

    fun getRecords(user: AccountData) {
        viewModelScope.launch {
            val records = MutableLiveData<ArrayList<RecordInternetData?>>()
            usersRealtimeDatabaseRepository.getUserRecords(user, records)
            records.observeForever {
                val recordsSort = mutableListOf<AccountData>()
                for (record in it) {
                    if (record != null) {
                        recordsSort.add(
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
                recordsSort.sortBy { -it.records!!.split(';')[3].toInt() }
                userRecords.postValue(recordsSort)
            }
        }
    }
}
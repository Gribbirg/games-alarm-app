package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.data.data.AccountData
import com.example.smartalarm.data.db.getRecordsList
import com.example.smartalarm.data.repositories.AuthRepository
import com.example.smartalarm.data.repositories.UsersRealtimeDatabaseRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch

class ProfileFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository
    private val usersRealtimeDatabaseRepository = UsersRealtimeDatabaseRepository
    val currentUser: MutableLiveData<AccountData?> = MutableLiveData()
    val userRecords: MutableLiveData<List<AccountData>> = MutableLiveData()

    init {
        authRepository.currentAccount.observeForever {
            currentUser.postValue(if (it != null) AccountData(it) else null)
            viewModelScope.launch {
                usersRealtimeDatabaseRepository.addUser(if (it != null) AccountData(it) else null)
                getUserRecords()
            }
        }
    }

    fun handleAuthResult(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                viewModelScope.launch {
                    authRepository.setAccountData(account)
                }
            }
        }
    }

    fun singOut() {
        viewModelScope.launch {
            authRepository.singOut()
        }
    }

    fun getUserRecords() {
        if (currentUser.value != null) {
            viewModelScope.launch {
                val user = MutableLiveData<AccountData>()
                usersRealtimeDatabaseRepository.getUser(currentUser.value?.uid!!, user)
                user.observeForever {
                    val records = mutableListOf<AccountData>()
                        for (record in getRecordsList(it.records!!)) {
                            if (record != null) {
                                records.add(
                                    AccountData(
                                        it.uid,
                                        it.email,
                                        it.name,
                                        it.photo,
                                        record.toString()
                                    )
                                )
                            }
                    }
                    records.sortBy { -it.records!!.split(';')[2].toInt() }
                    userRecords.postValue(records)
                }
            }
        } else {
            userRecords.postValue(listOf())
        }
    }
}
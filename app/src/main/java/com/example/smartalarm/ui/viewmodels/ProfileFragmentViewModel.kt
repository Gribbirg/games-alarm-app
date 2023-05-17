package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.smartalarm.data.data.AccountData
import com.example.smartalarm.data.repositories.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task

class ProfileFragmentViewModel(application: Application) : AndroidViewModel(application) {

    val authRepository = AuthRepository()
    val currentUser: MutableLiveData<AccountData?> = MutableLiveData()

    init {
        authRepository.currentAccount.observeForever {
            currentUser.postValue(if (it != null) AccountData(it) else null)
        }
    }
//    fun getCurrentUser() = authRepository.getCurrentUser()

    fun handleAuthResult(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null)
                authRepository.setAccountData(account)
        }
    }
}
package com.example.smartalarm.data.repositories

import android.accounts.Account
import androidx.lifecycle.MutableLiveData
import com.example.smartalarm.data.data.AccountData
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    val currentAccount : MutableLiveData<FirebaseUser?> = MutableLiveData()

    init {
        getCurrentUser()
    }
    private fun getCurrentUser() {
        currentAccount.postValue(auth.currentUser)
    }

    suspend fun setAccountData(account: GoogleSignInAccount) = withContext(Dispatchers.IO) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                getCurrentUser()
            }
        }
    }

    suspend fun singOut() = withContext(Dispatchers.IO) {
        auth.signOut()
        getCurrentUser()
    }
}
package com.example.smartalarm.data.repositories

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    val currentAccount : MutableLiveData<FirebaseUser?> = MutableLiveData()

    init {
        getCurrentUser()
    }
    private fun getCurrentUser() {
        currentAccount.postValue(auth.currentUser)
    }

    fun setAccountData(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                getCurrentUser()
            }
        }
    }
}
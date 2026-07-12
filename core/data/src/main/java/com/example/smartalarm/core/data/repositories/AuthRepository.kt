package com.example.smartalarm.core.data.repositories

import android.app.Activity
import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    val currentAccount : MutableLiveData<FirebaseUser?> = MutableLiveData()

    /**
     * Клиент Google-авторизации для запуска интента входа.
     *
     * Строка default_web_client_id генерируется плагином google-services
     * только в модуле :app, поэтому из библиотечного модуля она доступна
     * не через R, а по имени ресурса в рантайме.
     *
     * @param activity активити, от имени которой выполняется вход
     */
    @SuppressLint("DiscouragedApi")
    fun getSignInClient(activity: Activity): GoogleSignInClient {
        val webClientIdRes = activity.resources.getIdentifier(
            "default_web_client_id", "string", activity.packageName
        )
        return GoogleSignIn.getClient(
            activity,
            GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(webClientIdRes))
                .requestEmail()
                .build()
        )
    }

    init {
        currentAccount.postValue(auth.currentUser)
    }
    fun getCurrentUser() = currentAccount.value

    suspend fun setAccountData(account: GoogleSignInAccount) = withContext(Dispatchers.IO) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                currentAccount.postValue(auth.currentUser)
            }
        }
    }

    suspend fun singOut() = withContext(Dispatchers.IO) {
        auth.signOut()
        currentAccount.postValue(auth.currentUser)
    }
}
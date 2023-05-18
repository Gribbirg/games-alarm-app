package com.example.smartalarm.data.repositories

import android.util.Log
import com.example.smartalarm.data.data.AccountData
import com.example.smartalarm.data.db.ALL_GAMES
import com.example.smartalarm.data.db.GAMES_COUNT
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsersRealtimeDatabaseRepository {
    private val database = FirebaseDatabase.getInstance("https://smartalarm-ccdbb-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users")

    suspend fun addUser(account: AccountData?) = withContext(Dispatchers.IO) {
        if (account != null) {
            database.child(account.uid!!).get().addOnSuccessListener {
                if (!it.exists())
                    database.child(account.uid!!).setValue(account)
            }
        }
    }
}
package com.example.smartalarm.data.data

import android.net.Uri
import com.example.smartalarm.data.db.ALL_GAMES
import com.example.smartalarm.data.db.GAMES_COUNT
import com.example.smartalarm.data.db.GameData
import com.example.smartalarm.data.db.RecordsData
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser

class AccountData(
    var uid: String? = null,
    var email: String? = null,
    var name: String? = null,
    var photo: String? = null,
    recordsList: ArrayList<GameData?>? = null
) {
    var records = ""

    init {
        if (recordsList != null) {
            for (i in recordsList) {
                records += i?.toString() ?: "null"
                records += '/'
            }
        } else {
            for (i in ALL_GAMES.indices) {
                records += "null/"
            }
        }
    }

    constructor(account: FirebaseUser,  recordsList: ArrayList<GameData?>? = null) : this(
        account.uid,
        account.email!!,
        account.displayName!!,
        account.photoUrl!!.toString(),
        recordsList
    )

//    fun getRecordsList(): ArrayList<GameData?> {
//        val res = ArrayList<GameData?>()
//        for (i in records.split('/')) {
//            res.add(
//                if (i == "null") null else GameData(i)
//            )
//        }
//        return res
//    }
}
package com.example.smartalarm.data.data

import com.google.firebase.auth.FirebaseUser

class AccountData(
    var uid: String? = null,
    var email: String? = null,
    var name: String? = null,
    var photo: String? = null,
    var records: String? = ""
) {
    constructor(account: FirebaseUser,  recordsList: ArrayList<RecordInternetData?>? = null) : this(
        account.uid,
        account.email!!,
        account.displayName!!,
        account.photoUrl!!.toString(),
        recordsList
    )

    constructor(
        uid: String? = null,
        email: String? = null,
        name: String? = null,
        photo: String? = null,
        recordsList: ArrayList<RecordInternetData?>? = null
    ) : this (uid, email, name, photo, "") {
        if (recordsList != null) {
            for (i in recordsList) {
                records += i?.toString() ?: "null"
                records += '/'
            }
        } else {
            records = "null/"
        }
        records = records?.substring(0, records?.length!! - 1)
    }
}
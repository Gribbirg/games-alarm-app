package com.example.smartalarm.data.data

import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser

class AccountData(
    var email: String,
    var name: String,
    var photo: Uri
) {

    constructor(account: FirebaseUser) : this(
        account.email!!,
        account.displayName!!,
        account.photoUrl!!
    )
}
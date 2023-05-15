package com.example.smartalarm.data.auth

import android.content.SharedPreferences

class VKAccountService(val sharedPreference: SharedPreferences) : IAccountService {

    override var token: String?
        get() = sharedPreference.getString("token", null)
        set(value) {
            with(sharedPreference.edit()) {
                if (value == null) {
                    remove("token")
                }
                else {
                    putString("token", value)
                }
                apply()
            }
        }

    override var userId: String?
        get() = sharedPreference.getString("userId", null)
        set(value) {
            with(sharedPreference.edit()) {
                if (value == null) {
                    remove("userId")
                }
                else {
                    putString("userId", value)
                }
                apply()
            }
        }

}
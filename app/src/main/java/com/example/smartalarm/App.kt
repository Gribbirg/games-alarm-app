package com.example.smartalarm

import android.app.Application
import retrofit2.Retrofit

class App : Application() {

    lateinit var accountService: IAccountService

    lateinit var retrofit: Retrofit

    companion object {
        lateinit var application: App
    }

    override fun onCreate() {
        super.onCreate()
        application = this

    }
}
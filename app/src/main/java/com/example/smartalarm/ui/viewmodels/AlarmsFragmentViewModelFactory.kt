package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartalarm.data.AlarmsDao

class AlarmsFragmentViewModelFactory (
    private val dao : AlarmsDao,
    private val application: Application
        ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmsFragmentViewModel::class.java)) {
            return AlarmsFragmentViewModel(dao, application) as T
        }
        throw java.lang.IllegalArgumentException("Unknown ViewModel class")
    }
}
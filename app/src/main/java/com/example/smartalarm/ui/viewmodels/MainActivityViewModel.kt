package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.smartalarm.data.repositories.checkForHoliday

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    fun holidayAlertNeed(isCompleted: Boolean) : Boolean {
        return !isCompleted && checkForHoliday() != 0
    }

    fun resetAlertNeed() : Boolean {
        return checkForHoliday() == 0
    }

    fun getHolidayText() : String =
        when (checkForHoliday()) {
            1 -> "Завтра"
            2 -> "Послезавтра"
            else -> ""
        }
}
package com.example.smartalarm.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.db.GameData
import com.example.smartalarm.data.repositories.AlarmDbRepository
import kotlinx.coroutines.launch

class RecordsFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val alarmDbRepository = AlarmDbRepository(
        AlarmsDB.getInstance(getApplication())?.alarmsDao()!!
    )

    val myRecordsData: MutableLiveData<List<GameData>> = MutableLiveData()

    fun getRecordsFromDb(byGames: Boolean) {
        viewModelScope.launch {
            if (byGames) {
                myRecordsData.postValue(alarmDbRepository.getGames())
            } else {

            }
        }
    }
}
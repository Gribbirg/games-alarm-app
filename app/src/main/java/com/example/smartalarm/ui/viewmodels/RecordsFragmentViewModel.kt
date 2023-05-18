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

    val myRecordsData: MutableLiveData<ArrayList<GameData>> = MutableLiveData()

    fun getRecordsFromDb(state: Int) {
        viewModelScope.launch {
            when (state) {
                0 -> myRecordsData.postValue(ArrayList(alarmDbRepository.getGames()))
                1 -> {
                    val fromDb = alarmDbRepository.getRecordsByDate()
                    val res: ArrayList<GameData> = ArrayList()
                    for (record in fromDb)
                        res.add(GameData(record))
                    myRecordsData.postValue(res)
                }
            }
        }
    }
}
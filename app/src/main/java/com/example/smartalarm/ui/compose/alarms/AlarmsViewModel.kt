package com.example.smartalarm.ui.compose.alarms

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.data.data.WeekCalendarData
import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.repositories.AlarmCreateRepository
import com.example.smartalarm.data.repositories.AlarmDbRepository
import com.example.smartalarm.data.repositories.CalendarRepository
import com.example.smartalarm.data.repositories.getDayOfWeekNameVinit
import com.example.smartalarm.data.repositories.getMontNameVinit
import com.example.smartalarm.data.repositories.getTodayNumInWeek
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlarmsViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(AlarmsState())
    val state = _state.asStateFlow()

    var currentDayOfWeek: Int? = getTodayNumInWeek()
    private val calendarRepository = CalendarRepository()
    private val alarmCreateRepository = AlarmCreateRepository(application.applicationContext)
    var weekCalendarData: MutableLiveData<WeekCalendarData> = MutableLiveData()

    var alarmsList: MutableLiveData<ArrayList<AlarmData>> = MutableLiveData()
    var earliestAlarmsList: MutableLiveData<ArrayList<AlarmSimpleData?>> = MutableLiveData()

    private val alarmDbRepository = AlarmDbRepository(
        AlarmsDB.getInstance(getApplication())?.alarmsDao()!!
    )

    fun setWeekData() {
        weekCalendarData.postValue(calendarRepository.getWeek())
    }

    fun setDayOfWeek(dayOfWeek: Int) {
        currentDayOfWeek = dayOfWeek
        getAlarmsFromDbByDayOfWeek(currentDayOfWeek)
    }

    fun getAlarmsFromDbByDayOfWeek(dayOfWeek: Int? = currentDayOfWeek) {
        viewModelScope.launch {
            if (dayOfWeek == null || currentDayOfWeek == null)
                alarmsList.postValue(ArrayList())
            else {
                alarmsList.postValue(
                    alarmDbRepository.getAlarmsFromDbByDayOfWeek(
                        dayOfWeek,
                        weekCalendarData.value!!.daysList[currentDayOfWeek!!].toString()
                    )
                )
            }
        }
    }

    fun getEarliestAlarmsForAllWeek() {
        viewModelScope.launch {
            earliestAlarmsList.postValue(alarmDbRepository.getEarliestAlarmsFromDb(weekCalendarData.value!!.toStringArray()))
        }
    }

    fun setAlarmState(alarm: AlarmSimpleData) {
        viewModelScope.launch {
            alarmDbRepository.updateAlarmInDb(alarm)
            if (alarm.isOn) {
                alarmCreateRepository.create(AlarmData(alarm))
            } else {
                alarmCreateRepository.cancel(AlarmData(alarm))
            }
            getEarliestAlarmsForAllWeek()
        }
    }

    fun deleteAlarmFromDb(alarm: AlarmSimpleData) {
        viewModelScope.launch {
            alarmDbRepository.deleteAlarmFromDb(alarm)
            AlarmData(alarm, arrayListOf()).let(alarmCreateRepository::cancel)
            getAlarmsFromDbByDayOfWeek(currentDayOfWeek)
            getEarliestAlarmsForAllWeek()
        }
    }

    fun timesToString(alarmsList: ArrayList<AlarmSimpleData?>): ArrayList<String> {
        return com.example.smartalarm.data.repositories.timesToString(alarmsList)
    }

    fun addInfoInformationToBundle(currentBundle: Bundle?, id: Long? = null): Bundle {
        val resultBundle = currentBundle ?: Bundle()
        with(resultBundle) {
            putIntegerArrayList(
                "currentDay", arrayListOf(
                    currentDayOfWeek!!,
                    weekCalendarData.value!!.weekOfYear,
                    weekCalendarData.value!!.daysList[currentDayOfWeek!!].yearNumber
                )
            )
            putStringArrayList("infoCurrentDay", getCurrentDateStringForAllWeek())
            putStringArrayList("infoCurrentDayOfWeek", getDateOfWeekStringForAllWeek())
            putStringArrayList("datesOfWeek", weekCalendarData.value!!.toStringArray())
            putBoolean("isNew", id == null)
            if (id != null) putLong("alarmId", id)
        }
        return resultBundle
    }

    fun changeWeek(next: Int) {
        currentDayOfWeek = null
        alarmsList.postValue(ArrayList())
        calendarRepository.changeWeek(next)
        weekCalendarData.postValue(calendarRepository.getWeek())
        getEarliestAlarmsForAllWeek()
    }

    fun setDate(dayInfo: ArrayList<Int>) {
        currentDayOfWeek = dayInfo[0]
        weekCalendarData.postValue(calendarRepository.getWeekOfDay(dayInfo[1], dayInfo[2]))
    }

    fun getInfoLine() =
        if (currentDayOfWeek == null)
            "Выберите день"
        else
            "Будильники на ${getCurrentDateOfWeekString(currentDayOfWeek!!)},\n${
                getCurrentDateString(
                    currentDayOfWeek!!
                )
            }:"

    private fun getCurrentDateString(dayOfWeek: Int) =
        weekCalendarData.value!!.daysList[dayOfWeek].dayNumber.toString() + " " +
                getMontNameVinit(weekCalendarData.value!!.daysList[dayOfWeek].monthNumber)

    private fun getCurrentDateOfWeekString(dayOfWeek: Int) =
        if (weekCalendarData.value!!.daysList[dayOfWeek].today)
            "сегодня"
        else if (calendarRepository.isAhead(dayOfWeek, 1))
            "завтра"
        else if (calendarRepository.isAhead(dayOfWeek, 2))
            "послезавтра"
        else
            getDayOfWeekNameVinit(dayOfWeek)

    private fun getCurrentDateStringForAllWeek(): ArrayList<String> {
        val list = ArrayList<String>()
        for (i in 0..6)
            list.add(getCurrentDateString(i))
        return list
    }

    private fun getDateOfWeekStringForAllWeek(): ArrayList<String> {
        val list = ArrayList<String>()
        for (i in 0..6)
            list.add(getCurrentDateOfWeekString(i))
        return list
    }
}
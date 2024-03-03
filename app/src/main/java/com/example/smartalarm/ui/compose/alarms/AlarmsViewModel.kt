package com.example.smartalarm.ui.compose.alarms

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.data.data.Date
import com.example.smartalarm.data.data.WeekCalendarData
import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.repositories.AlarmCreateRepository
import com.example.smartalarm.data.repositories.AlarmDbRepository
import com.example.smartalarm.data.repositories.CalendarIsAhead
import com.example.smartalarm.data.repositories.CalendarRepository
import com.example.smartalarm.data.repositories.getDayOfWeekNameVinit
import com.example.smartalarm.data.repositories.getDefaultWeekDataList
import com.example.smartalarm.data.repositories.getMontNameVinit
import com.example.smartalarm.data.repositories.getToday
import com.example.smartalarm.data.repositories.getTodayNumInWeek
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.AlarmsListErrorState
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.AlarmsListLoadedState
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.AlarmsListLoadingState
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.AlarmsListState
import com.example.smartalarm.ui.compose.alarms.view.calendar.CalendarViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlarmsViewModel(application: Application) : AndroidViewModel(application),
    OnAlarmsScreenClickListener {

    private val _state = MutableStateFlow(
        AlarmsState(
            alarmsListState = AlarmsListLoadingState(getToday().dayOfWeek),
            calendarViewState = CalendarViewState(
                getDefaultWeekDataList(100),
                getToday().dayOfWeek
            ),
            dayInfoText = getInfoLine(getToday())
        )
    )
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

    init {
        viewModelScope.launch {
            try {
                _state.update {
                    it.copy(
                        alarmsListState = AlarmsListLoadedState(
                            it.alarmsListState.dayNum,
                            alarmDbRepository.getAlarmsList()
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("db", "Database: error on alarms list loading: $e")
                _state.update {
                    it.copy(
                        alarmsListState = AlarmsListErrorState(
                            it.alarmsListState.dayNum,
                            e.toString()
                        )
                    )
                }
            }
        }
    }

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
                    state
                        .value
                        .calendarViewState
                        .data[state.value.calendarViewState.selectedDayNum / 7]
                        .daysList[state.value.calendarViewState.selectedDayNum % 7]
                        .dayOfWeek,
//                    weekCalendarData.value!!.weekOfYear,
                    state
                        .value
                        .calendarViewState.data[state.value.calendarViewState.selectedDayNum / 7]
                        .daysList[state.value.calendarViewState.selectedDayNum % 7]
                        .yearNumber
                )
            )
            putStringArrayList("infoCurrentDay", getCurrentDateStringForAllWeek())
            putStringArrayList("infoCurrentDayOfWeek", getDateOfWeekStringForAllWeek())
//            putStringArrayList("datesOfWeek", weekCalendarData.value!!.toStringArray())
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

    private fun getInfoLine(day: Date) =
        "Будильники на ${getCurrentDateOfWeekString(day)},\n${
            getCurrentDateString(day)
        }:"

    private fun getCurrentDateString(day: Date) =
        day.dayNumber.toString() + " " + getMontNameVinit(day.monthNumber)

    private fun getCurrentDateOfWeekString(day: Date): String =
        when (val ahead = CalendarRepository.isAhead(day)) {
            CalendarIsAhead.FAR -> getDayOfWeekNameVinit(day.dayOfWeek)
            else -> ahead.toString()
        }
//            getDayOfWeekNameVinit(day.dayOfWeek)

    private fun getCurrentDateStringForAllWeek(): ArrayList<String> {
        val list = ArrayList<String>()
        val week =
            CalendarRepository.getWeek(
                state
                    .value
                    .calendarViewState
                    .data[state.value.calendarViewState.selectedDayNum / 7]
                    .daysList[state.value.calendarViewState.selectedDayNum % 7]
            )
        for (day in week)
            list.add(getCurrentDateString(day))
        return list
    }

    private fun getDateOfWeekStringForAllWeek(): ArrayList<String> {
        val list = ArrayList<String>()
        val week =
            CalendarRepository.getWeek(
                state
                    .value
                    .calendarViewState
                    .data[state.value.calendarViewState.selectedDayNum / 7]
                    .daysList[state.value.calendarViewState.selectedDayNum % 7]
            )
        for (day in week)
            list.add(getCurrentDateOfWeekString(day))
        return list
    }

    override fun onDayViewClick(dayNum: Int) {
        viewModelScope.launch {
            _state.update { state ->
                state.copy(
                    alarmsListState = state.alarmsListState.copy(dayNum),
                    calendarViewState = state.calendarViewState.copy(
                        selectedDayNum = dayNum
                    ),
                    dayInfoText = getInfoLine(
                        state.calendarViewState.data[dayNum / 7].daysList[dayNum % 7]
                    )
                )
            }
        }
    }

    override fun pagerScroll(dayNum: Int) {
        onDayViewClick(dayNum)
    }
}

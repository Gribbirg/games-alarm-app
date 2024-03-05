package com.example.smartalarm.ui.compose.alarms

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.App
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
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.AlarmsListEvent
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.AlarmsListLoadedState
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.AlarmsListLoadingState
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.AlarmsListPagerScrollEvent
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.item.AlarmsListItemChangeEvent
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.item.AlarmsListItemEvent
import com.example.smartalarm.ui.compose.alarms.view.alarmslist.item.AlarmsListItemSetOnStateEvent
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetCloseEvent
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetEvent
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetOffState
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetOnCopyClickedEvent
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetOnDeleteClickedEvent
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetOnEditClickedEvent
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetOnState
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetState
import com.example.smartalarm.ui.compose.alarms.view.calendar.CalendarViewEvent
import com.example.smartalarm.ui.compose.alarms.view.calendar.CalendarViewState
import com.example.smartalarm.ui.compose.alarms.view.calendar.calendarday.CalendarDayEvent
import com.example.smartalarm.ui.compose.alarms.view.calendar.calendarday.CalendarDayOnClickEvent
import com.example.smartalarm.ui.compose.alarms.view.calendar.calendarday.CalendarDayState
import com.example.smartalarm.ui.compose.alarms.view.deletedialog.AlarmDeleteDialogConfirmEvent
import com.example.smartalarm.ui.compose.alarms.view.deletedialog.AlarmDeleteDialogDismissEvent
import com.example.smartalarm.ui.compose.alarms.view.deletedialog.AlarmDeleteDialogEvent
import com.example.smartalarm.ui.compose.alarms.view.deletedialog.AlarmDeleteDialogOffState
import com.example.smartalarm.ui.compose.alarms.view.deletedialog.AlarmDeleteDialogOnState
import com.example.smartalarm.ui.compose.alarms.view.deletedialog.AlarmDeleteDialogState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.days

class AlarmsViewModel(application: Application) : AndroidViewModel(application) {

    private val weekCalendarData: List<WeekCalendarData> = getDefaultWeekDataList(100)

    private val _state = MutableStateFlow(getDefaultState())
    val state = _state.asStateFlow()

    private val creator = AlarmCreateRepository(application.applicationContext)
    private val alarmCreateRepository = AlarmCreateRepository(application.applicationContext)

    var currentDayOfWeek: Int? = getTodayNumInWeek()
    private val calendarRepository = CalendarRepository()
//    var weekCalendarData: MutableLiveData<WeekCalendarData> = MutableLiveData()

    var alarmsList: MutableLiveData<ArrayList<AlarmData>> = MutableLiveData()
    var earliestAlarmsList: MutableLiveData<ArrayList<AlarmSimpleData?>> = MutableLiveData()

    private val alarmDbRepository = AlarmDbRepository(
        AlarmsDB.getInstance(getApplication())?.alarmsDao()!!
    )

    init {
        viewModelScope.launch {
            alarmsListLoad()
        }
    }


    private fun getDefaultState(): AlarmsState {
        val today = getToday()
        return AlarmsState(
            alarmsListState = AlarmsListLoadingState(today.dayOfWeek),
            calendarViewState = getCalendarViewState(today.dayOfWeek),
            bottomSheetState = AlarmEditBottomSheetOffState(),
            deleteDialogState = AlarmDeleteDialogOffState(),
            snackBarState = SnackBarOffState(),
            dayInfoText = getInfoLine(today)
        )
    }

    private fun getCalendarViewState(selectedDayNum: Int): CalendarViewState {
        return CalendarViewState(
            days = weekCalendarData.mapIndexed { weekNum, week ->
                week.daysList.mapIndexed { dayNum, day ->
                    val num = weekNum * 7 + dayNum
                    CalendarDayState(
                        day,
                        num,
                        "",
                        selectedDayNum == num
                    )
                }
            },
            monthTextList = weekCalendarData.map { element -> element.monthList },
            selectedDayNum = selectedDayNum
        )
    }

    fun onEvent(event: AlarmsEvent) {
        viewModelScope.launch {
            when (event) {
                is AlarmsListEvent -> onAlarmsListEvent(event)
                is CalendarViewEvent -> onCalendarViewEvent(event)
                is AlarmEditBottomSheetEvent -> onAlarmEditBottomSheetEvent(event)
                is AlarmDeleteDialogEvent -> onAlarmDeleteDialogEvent(event)
                is SnackBarEvent -> onSnackBarEvent(event)
            }
        }
    }

    private suspend fun onAlarmsListEvent(event: AlarmsListEvent) = withContext(Dispatchers.IO) {
        when (event) {
            is AlarmsListPagerScrollEvent -> onDayChange(event.dayNum)
            is AlarmsListItemEvent -> onAlarmsListItemEvent(event)
        }
    }

    private suspend fun onAlarmsListItemEvent(event: AlarmsListItemEvent) =
        withContext(Dispatchers.IO) {
            when (event) {
                is AlarmsListItemSetOnStateEvent -> {
                    val newAlarm = event.alarm.copy(
                        isOn = event.isOn
                    )
                    setAlarmState(
                        AlarmSimpleData(
                            newAlarm
                        )
                    )
                }

                is AlarmsListItemChangeEvent -> {
                    bottomSheetStateChange(alarm = event.alarm)
                }
            }
        }

    private suspend fun onCalendarViewEvent(event: CalendarViewEvent) =
        withContext(Dispatchers.IO) {
            when (event) {
                is CalendarDayEvent -> onCalendarDayEvent(event)
            }
        }

    private suspend fun onCalendarDayEvent(event: CalendarDayEvent) = withContext(Dispatchers.IO) {
        when (event) {
            is CalendarDayOnClickEvent -> onDayChange(event.dayNum)
        }
    }

    private suspend fun onAlarmEditBottomSheetEvent(event: AlarmEditBottomSheetEvent) =
        withContext(Dispatchers.IO) {
            when (event) {
                is AlarmEditBottomSheetCloseEvent -> bottomSheetStateChange(null)
                is AlarmEditBottomSheetOnDeleteClickedEvent -> {
                    deleteDialogStateChange(event.alarm)
                }
                is AlarmEditBottomSheetOnCopyClickedEvent -> {
                    snackBarStateChange(SnackBarAlarmCopyState(event.alarm))
                    bottomSheetStateChange()
                }
                is AlarmEditBottomSheetOnEditClickedEvent -> {

                }
                else -> {}
            }
        }

    private suspend fun onAlarmDeleteDialogEvent(event: AlarmDeleteDialogEvent) =
        withContext(Dispatchers.IO) {
            when (event) {
                is AlarmDeleteDialogConfirmEvent -> {
                    deleteAlarmFromDb(AlarmSimpleData(event.alarm))
                    bottomSheetStateChange(null)
                    deleteDialogStateChange()
                    snackBarStateChange(SnackBarAlarmDeleteState(event.alarm))
                }

                is AlarmDeleteDialogDismissEvent -> {
                    deleteDialogStateChange()
                }
            }
        }

    private suspend fun onSnackBarEvent(event: SnackBarEvent) = withContext(Dispatchers.IO) {
        when (event) {
            is SnackBarDismissEvent -> snackBarStateChange(SnackBarOffState())
            is SnackBarAlarmReturnEvent -> {
                alarmDbRepository.insertAlarmToDb(event.alarm)
                event.alarm.let(creator::create)
                alarmsListLoad()
                snackBarStateChange(SnackBarOffState())
            }
        }
    }

    private suspend fun onDayChange(dayNum: Int) = withContext(Dispatchers.IO) {
        _state.update { state ->

            val oldDayNum = state.calendarViewState.selectedDayNum
            val days = state.calendarViewState.days
            days[oldDayNum / 7][oldDayNum % 7].isSelected = false
            days[dayNum / 7][dayNum % 7].isSelected = true

            state.copy(
                alarmsListState = state.alarmsListState.copy(dayNum),
                calendarViewState = state.calendarViewState.copy(
                    selectedDayNum = dayNum,
                    days = days
                ),
                dayInfoText = getInfoLine(
                    weekCalendarData[dayNum / 7].daysList[dayNum % 7]
                )
            )
        }
    }

    private suspend fun alarmsListLoad() = withContext(Dispatchers.IO) {
        try {
            val alarmsList = alarmDbRepository.getAlarmsList()
            _state.update {
                it.copy(
                    alarmsListState = AlarmsListLoadedState(
                        it.alarmsListState.dayNum,
                        alarmsList
                    ),
                    calendarViewState = it.calendarViewState.copy(
                        days = it.calendarViewState.days.map { week ->
                            week.mapIndexed { dayNum, day ->
                                day.copy(
                                    earliestAlarm = alarmsList[dayNum].find { alarm -> alarm.isOn }
                                        ?.getTime() ?: ""
                                )
                            }
                        }
                    )
                )
            }
        } catch (e: Exception) {
            onAlarmsDbError(e)
        }
    }

//    fun getEarliestAlarmsForAllWeek() {
//        viewModelScope.launch {
//            earliestAlarmsList.postValue(alarmDbRepository.getEarliestAlarmsFromDb(weekCalendarData.value!!.toStringArray()))
//        }
//    }

    private suspend fun setAlarmState(alarm: AlarmSimpleData) = withContext(Dispatchers.IO) {
        try {
            alarmDbRepository.updateAlarmInDb(alarm)
            if (alarm.isOn) {
                alarmCreateRepository.create(AlarmData(alarm))
            } else {
                alarmCreateRepository.cancel(AlarmData(alarm))
            }
            alarmsListLoad()
        } catch (e: Exception) {
            onAlarmsDbError(e)
        }
//            getEarliestAlarmsForAllWeek()
    }

    private suspend fun onAlarmsDbError(e: Exception) = withContext(Dispatchers.IO) {
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

    private suspend fun bottomSheetStateChange(alarm: AlarmData? = null) =
        withContext(Dispatchers.IO) {
            _state.update { state ->
                state.copy(
                    bottomSheetState =
                    if (alarm != null) AlarmEditBottomSheetOnState(alarm)
                    else AlarmEditBottomSheetOffState()
                )
            }
        }

    private suspend fun deleteDialogStateChange(alarm: AlarmData? = null) =
        withContext(Dispatchers.IO) {
            _state.update { state ->
                state.copy(
                    deleteDialogState =
                    if (alarm != null) AlarmDeleteDialogOnState(alarm)
                    else AlarmDeleteDialogOffState()
                )
            }
        }

    private suspend fun snackBarStateChange(snackBarState: SnackBarState) =
        withContext(Dispatchers.IO) {
            _state.update { state ->
                state.copy(
                    snackBarState = snackBarState
                )
            }
        }

    private suspend fun deleteAlarmFromDb(alarm: AlarmSimpleData) = withContext(Dispatchers.IO) {
        viewModelScope.launch {
            alarmDbRepository.deleteAlarmFromDb(alarm)
            AlarmData(alarm, arrayListOf()).let(alarmCreateRepository::cancel)
            alarmsListLoad()
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
                    weekCalendarData[state.value.calendarViewState.selectedDayNum / 7]
                        .daysList[state.value.calendarViewState.selectedDayNum % 7]
                        .dayOfWeek,
//                    weekCalendarData.value!!.weekOfYear,
                    weekCalendarData[state.value.calendarViewState.selectedDayNum / 7]
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
                weekCalendarData[state.value.calendarViewState.selectedDayNum / 7]
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
                weekCalendarData[state.value.calendarViewState.selectedDayNum / 7]
                    .daysList[state.value.calendarViewState.selectedDayNum % 7]
            )
        for (day in week)
            list.add(getCurrentDateOfWeekString(day))
        return list
    }
}

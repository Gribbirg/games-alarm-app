package com.example.smartalarm.ui.compose.alarms

import android.app.Application
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.smartalarm.ui.compose.view.alarmitem.AlarmItemChangeEvent
import com.example.smartalarm.ui.compose.view.alarmitem.AlarmItemEvent
import com.example.smartalarm.ui.compose.view.alarmitem.AlarmItemSetOnStateEvent
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetCloseEvent
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetEvent
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetOffState
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetOnCopyClickedEvent
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetOnDeleteClickedEvent
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetOnEditClickedEvent
import com.example.smartalarm.ui.compose.alarms.view.bottomsheet.AlarmEditBottomSheetOnState
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
import com.example.smartalarm.ui.compose.view.alarmitem.AlarmItemClockClickedEvent
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogDismissEvent
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogEvent
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogOffState
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogOnState
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogSetEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            alarmsSnackBarState = AlarmsSnackBarOffState(),
            timePickerState = TimePickerDialogOffState(),
            dayInfoText = getInfoLine(today),
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
        when (event) {
            is AlarmsListEvent -> onAlarmsListEvent(event)
            is CalendarViewEvent -> onCalendarViewEvent(event)
            is AlarmEditBottomSheetEvent -> onAlarmEditBottomSheetEvent(event)
            is AlarmDeleteDialogEvent -> onAlarmDeleteDialogEvent(event)
            is SnackBarEvent -> onSnackBarEvent(event)
        }
    }

    private fun onAlarmsListEvent(event: AlarmsListEvent) {
        when (event) {
            is AlarmsListPagerScrollEvent -> onDayChange(event.dayNum)
        }
    }

    fun onAlarmsListItemEvent(event: AlarmItemEvent) {
        when (event) {
            is AlarmItemSetOnStateEvent -> {
                val newAlarm = event.alarm.copy(
                    isOn = event.isOn
                )
                setAlarmState(
                    AlarmSimpleData(
                        newAlarm
                    )
                )
            }

            is AlarmItemChangeEvent -> {
                bottomSheetStateChange(alarm = event.alarm)
            }

            is AlarmItemClockClickedEvent -> {
                _state.update { state ->
                    state.copy(
                        timePickerState = TimePickerDialogOnState(event.alarm)
                    )
                }
            }
        }
    }

    fun onTimePickerDialogEvent(event: TimePickerDialogEvent) {
        viewModelScope.launch {
            when (event) {
                is TimePickerDialogDismissEvent -> {
                    _state.update { state ->
                        state.copy(
                            timePickerState = TimePickerDialogOffState()
                        )
                    }
                }

                is TimePickerDialogSetEvent -> {
                    _state.update { state ->
                        state.copy(
                            timePickerState = TimePickerDialogOffState()
                        )
                    }
                    try {
                        val alarm = event.alarm

                        creator.cancel(alarm)

                        alarm.timeHour = event.hour
                        alarm.timeMinute = event.minute
                        alarmDbRepository.updateAlarmInDb(AlarmSimpleData(alarm))
                        creator.create(alarm)

                        alarmsListLoad()
                        snackBarStateChange(AlarmsSnackBarTimeChangeState(alarm))
                    } catch (e: Exception) {
                        onAlarmsDbError(e)
                    }
                }
            }
        }
    }

    private fun onCalendarViewEvent(event: CalendarViewEvent) {
        when (event) {
            is CalendarDayEvent -> onCalendarDayEvent(event)
        }
    }

    private fun onCalendarDayEvent(event: CalendarDayEvent) {
        when (event) {
            is CalendarDayOnClickEvent -> onDayChange(event.dayNum)
        }
    }

    private fun onAlarmEditBottomSheetEvent(event: AlarmEditBottomSheetEvent) {
        when (event) {
            is AlarmEditBottomSheetCloseEvent -> bottomSheetStateChange(null)
            is AlarmEditBottomSheetOnDeleteClickedEvent -> {
                deleteDialogStateChange(event.alarm)
            }

            is AlarmEditBottomSheetOnCopyClickedEvent -> {
                snackBarStateChange(AlarmsSnackBarAlarmCopyState(event.alarm))
                bottomSheetStateChange()
            }

            is AlarmEditBottomSheetOnEditClickedEvent -> {
                bottomSheetStateChange()
            }

            else -> {}
        }
    }

    private fun onAlarmDeleteDialogEvent(event: AlarmDeleteDialogEvent) {
        when (event) {
            is AlarmDeleteDialogConfirmEvent -> {
                viewModelScope.launch {
                    deleteAlarmFromDb(AlarmSimpleData(event.alarm))
                    bottomSheetStateChange(null)
                    deleteDialogStateChange()
                    snackBarStateChange(AlarmsSnackBarAlarmDeleteState(event.alarm))
                }
            }

            is AlarmDeleteDialogDismissEvent -> {
                deleteDialogStateChange()
            }
        }
    }

    private fun onSnackBarEvent(event: SnackBarEvent) {
        viewModelScope.launch {
            when (event) {
                is SnackBarDismissEvent -> snackBarStateChange(AlarmsSnackBarOffState())
                is SnackBarAlarmReturnEvent -> {
                    alarmDbRepository.insertAlarmToDb(event.alarm)
                    event.alarm.let(creator::create)
                    alarmsListLoad()
                    snackBarStateChange(AlarmsSnackBarOffState())
                }
            }
        }
    }

    private fun onDayChange(dayNum: Int) {
        viewModelScope.launch {
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
    }

    private fun alarmsListLoad() {
        viewModelScope.launch {
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
    }

//    fun getEarliestAlarmsForAllWeek() {
//        viewModelScope.launch {
//            earliestAlarmsList.postValue(alarmDbRepository.getEarliestAlarmsFromDb(weekCalendarData.value!!.toStringArray()))
//        }
//    }

    private fun setAlarmState(alarm: AlarmSimpleData) {
        viewModelScope.launch {
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

    private fun bottomSheetStateChange(alarm: AlarmData? = null) {
        _state.update { state ->
            state.copy(
                bottomSheetState =
                if (alarm != null) AlarmEditBottomSheetOnState(alarm)
                else AlarmEditBottomSheetOffState()
            )
        }
    }

    private fun deleteDialogStateChange(alarm: AlarmData? = null) {
        _state.update { state ->
            state.copy(
                deleteDialogState =
                if (alarm != null) AlarmDeleteDialogOnState(alarm)
                else AlarmDeleteDialogOffState()
            )
        }
    }

    private fun snackBarStateChange(alarmsSnackBarState: AlarmsSnackBarState) {
        viewModelScope.launch {
            _state.update { state ->
                state.copy(
                    alarmsSnackBarState = alarmsSnackBarState
                )
            }
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

    fun refresh(dayOfWeek: Int = state.value.alarmsListState.dayNum % 7) {
        viewModelScope.launch {
            alarmsListLoad()

            val current = state.value.alarmsListState.dayNum
            onDayChange(current / 7 + dayOfWeek)
        }
    }

    fun afterAlarmChange(isNew: Boolean, alarm: AlarmData) {
        viewModelScope.launch {
            _state.update { state ->
                state.copy(
                    alarmsSnackBarState =
                    if (isNew) AlarmsSnackBarCreatedState(alarm)
                    else AlarmsSnackBarEditedState(alarm)
                )
            }
        }
    }
}

package com.example.smartalarm.ui.compose.addalarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.data.repositories.AlarmCreateRepository
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.data.constants.ALL_GAMES
import com.example.smartalarm.data.db.AlarmSimpleData
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.repositories.AlarmDbRepository
import com.example.smartalarm.data.repositories.isAhead
import com.example.smartalarm.ui.compose.view.alarmitem.AlarmItemClockClickedEvent
import com.example.smartalarm.ui.compose.view.alarmitem.AlarmItemEvent
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogDismissEvent
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogEvent
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogOffState
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogOnState
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogSetEvent
import com.example.smartalarm.ui.compose.view.timepickerdialog.TimePickerDialogState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddAlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(getDefaultState())
    val state = _state.asStateFlow()

    var currentAlarm: AlarmData? = null
    var gamesList: ArrayList<Int> = ArrayList()
    private val creator = AlarmCreateRepository(application.applicationContext)

    init {
        for (i in ALL_GAMES.indices)
            gamesList.add(1)
    }

    private fun getDefaultState() = AddAlarmState(
        alarm = AlarmData(),
        isNew = true,
        daysOfWeek = MutableList(7) { false },
        timePickerDialogState = TimePickerDialogOffState(),
    )

    private val alarmDbRepository = AlarmDbRepository(
        AlarmsDB.getInstance(getApplication())?.alarmsDao()!!
    )

    fun onEvent(event: AddAlarmEvent) {
            when (event) {
                is AddAlarmTimeClickedEvent -> launchTimePickerDialog()
                is AddAlarmDayOfWeekSelectedEvent -> {
                    _state.update { state ->
                        state.copy(
                            daysOfWeek = MutableList(7) { i ->
                                if (i == event.dayOfWeek)
                                    event.isOn
                                else
                                    state.daysOfWeek[i]
                            }
                        )
                    }
                }

                is AddAlarmNameChangeEvent -> {
                    setAlarm(
                        state.value.alarm.copy(
                            name = event.name
                        )
                    )
                }

                is AddAlarmVibrationChangeEvent -> {
                    setAlarm(
                        state.value.alarm.copy(
                            isVibration = event.isVibration
                        )
                    )
                }
            }
    }

    fun onAlarmItemEvent(event: AlarmItemEvent) {
        viewModelScope.launch {
            when (event) {
                is AlarmItemClockClickedEvent -> launchTimePickerDialog()
            }
        }
    }

    fun onTimePickerDialogEvent(event: TimePickerDialogEvent) {
        viewModelScope.launch {
            when (event) {
                is TimePickerDialogDismissEvent -> {
                    _state.update { state ->
                        state.copy(
                            timePickerDialogState = TimePickerDialogOffState()
                        )
                    }
                }

                is TimePickerDialogSetEvent -> {
                    _state.update { state ->
                        state.copy(
                            timePickerDialogState = TimePickerDialogOffState(),
                            alarm = state.alarm.copy(
                                timeHour = event.hour,
                                timeMinute = event.minute
                            )
                        )
                    }
                }
            }
        }
    }

    private fun launchTimePickerDialog() {
        _state.update { state ->
            state.copy(
                timePickerDialogState = TimePickerDialogOnState(state.alarm)
            )
        }
    }

    private fun setAlarm(alarm: AlarmData) {
        _state.update { state ->
            state.copy(
                alarm = alarm
            )
        }
    }

    fun insertOrUpdateAlarm(alarm: AlarmSimpleData): Boolean {
        if (alarm.activateDate != null)
            if (!isAhead(alarm.activateDate!!, alarm.timeHour, alarm.timeMinute))
                return false
        insertOrUpdateAlarmToDb(alarm)
        return true
    }

    private fun insertOrUpdateAlarmToDb(
        alarm: AlarmSimpleData
    ) {
        viewModelScope.launch {
            if (alarm.name == "") alarm.name = "Будильник"

            if (currentAlarm == null) {

                alarm.id = alarmDbRepository.insertAlarmToDb(AlarmData(alarm, gamesList))
                AlarmData(alarm, gamesList).let(creator::create)
            } else {
                alarm.id = currentAlarm!!.id

                if (
                    currentAlarm!!.timeMinute != alarm.timeMinute ||
                    currentAlarm!!.timeHour != alarm.timeHour ||
                    currentAlarm!!.dayOfWeek != alarm.dayOfWeek ||
                    currentAlarm!!.activateDate != alarm.activateDate
                ) {
                    alarm.recordSeconds = null
                    alarm.recordScore = null
                } else {
                    alarm.recordSeconds = currentAlarm!!.recordSeconds
                    alarm.recordScore = currentAlarm!!.recordScore
                }
                alarmDbRepository.updateAlarmInDbWithGames(AlarmData(alarm, gamesList))
                AlarmData(alarm, gamesList).let(creator::update)
            }
        }
    }

    suspend fun getAlarm(id: Long): AlarmData = withContext(Dispatchers.IO) {
        return@withContext alarmDbRepository.getAlarmWithGames(id)
    }
}
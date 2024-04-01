package com.example.smartalarm.ui.compose.addalarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.data.constants.ALL_GAMES
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.data.data.GameData
import com.example.smartalarm.data.db.AlarmsDB
import com.example.smartalarm.data.repositories.AlarmCreateRepository
import com.example.smartalarm.data.repositories.AlarmDbRepository
import com.example.smartalarm.data.repositories.CalendarRepository
import com.example.smartalarm.data.repositories.getTodayDate
import com.example.smartalarm.data.repositories.isAhead
import com.example.smartalarm.ui.compose.view.alarmitem.AlarmItemClockClickedEvent
import com.example.smartalarm.ui.compose.view.alarmitem.AlarmItemEvent
import com.example.smartalarm.ui.compose.view.alarmitem.AlarmItemSetOnStateEvent
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

class AddAlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(getDefaultState())
    val state = _state.asStateFlow()

    var copiedAlarm: AlarmData? = null
        set(value) {
            field = value?.also { it.id = 0L }

            _state.update { state ->
                state.copy(
                    hasCopiedAlarm = value != null
                )
            }
        }

    var gamesList: ArrayList<Int> = ArrayList()
    private val creator = AlarmCreateRepository(application.applicationContext)

    init {
        for (i in ALL_GAMES.indices)
            gamesList.add(1)
    }

    private fun getDefaultState() = AddAlarmState(
        alarm = AlarmData(),
        isNew = true,
        hasCopiedAlarm = copiedAlarm != null,
        daysOfWeek = MutableList(7) { false },
        selectedGamesList = listOf(),
        timePickerDialogState = TimePickerDialogOffState(),
        alertDialogState = AddAlarmAlertDialogOffState()
    )

    private val alarmDbRepository = AlarmDbRepository(
        AlarmsDB.getInstance(getApplication())?.alarmsDao()!!
    )

    fun onEvent(event: AddAlarmEvent) {
        when (event) {
            is AddAlarmTimeClickedEvent -> launchTimePickerDialog()

            is AddAlarmDatePickerEvent -> onDatePickerEvent(event)

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

            is AddAlarmAlertDialogCloseEvent -> {
                _state.update { state ->
                    state.copy(
                        alertDialogState = AddAlarmAlertDialogOffState()
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

            is AddAlarmRisingVolumeChangeEvent -> {
                setAlarm(
                    state.value.alarm.copy(
                        isRisingVolume = event.isRisingVolume
                    )
                )
            }

            is AddAlarmSaveEvent -> {
                viewModelScope.launch {
                    if (state.value.alarm.activateDate == null && state.value.daysOfWeek.all { !it }) {
                        _state.update { state ->
                            state.copy(
                                alertDialogState = AddAlarmAlertDialogTextState(
                                    head = "Выберите дни!",
                                    body = "Выберите дни недели, на которые будет установлен ${state.alarm.name}."
                                )
                            )
                        }
                    } else if (
                        state.value.alarm.let {
                            it.activateDate != null &&
                                    !isAhead(
                                        it.activateDate!!,
                                        it.timeHour,
                                        it.timeMinute
                                    )
                        }
                    ) {
                        _state.update { state ->
                            state.copy(
                                alertDialogState = AddAlarmAlertDialogTextState(
                                    head = "Нельзя установить будильник в прошлое!",
                                    body = "Измените время или дату будильника."
                                )
                            )
                        }
                    } else {
                        insertOrUpdateAlarmToDb(
                            state.value.isNew,
                            state.value.alarm,
                            state.value.daysOfWeek
                        )
                        _state.update { state ->
                            state.copy(
                                saveFinish = true
                            )
                        }
                    }
                }
            }

            is AddAlarmRingtoneSelectedEvent -> {
                _state.update { state ->
                    state.copy(
                        alarm = state.alarm.copy(
                            ringtonePath = event.ringtonePath
                        )
                    )
                }
            }

            is AddAlarmPasteEvent -> {
                viewModelScope.launch {
                    _state.update { state ->
                        if (copiedAlarm == null)
                            state.copy(
                                snackBarState = AddAlarmSnackBarTextAlertState("Нет скопированного будильника")
                            )
                        else
                            state.copy(
                                alarm = copiedAlarm!!,
                                snackBarState = AddAlarmSnackBarPastedState(copiedAlarm!!)
                            )
                    }
                }
            }

            is AddAlarmSnackBarClosedEvent -> {
                viewModelScope.launch {
                    _state.update { state ->
                        state.copy(
                            snackBarState = AddAlarmSnackBarOffState()
                        )
                    }
                }
            }

            is AddAlarmOneTimeChangeEvent -> {
                _state.update { state ->
                    state.copy(
                        isOneTime = event.isOn,
                        alarm = state.alarm.copy(
                            activateDate =
                            if (event.isOn) getTodayDate()
                            else null
                        )
                    )
                }
            }
        }
    }

    private fun onDatePickerEvent(event: AddAlarmDatePickerEvent) {
        when (event) {
            is AddAlarmDatePickerOpenEvent -> {
                _state.update { state ->
                    state.copy(
                        datePickerState = AddAlarmDatePickerOnState()
                    )
                }
            }

            is AddAlarmDatePickerCloseEvent -> {
                _state.update { state ->
                    state.copy(
                        datePickerState = AddAlarmDatePickerOffState()
                    )
                }
            }

            is AddAlarmDatePickerSaveEvent -> {

                if (event.dateInMillis != null && event.dateInMillis >= CalendarRepository.getTodayInMillis()) {
                    _state.update { state ->
                        state.copy(
                            alarm = state.alarm.copy(
                                activateDate = CalendarRepository.getDateByMillis(event.dateInMillis)
                            ),
                            datePickerState = AddAlarmDatePickerOffState()
                        )
                    }
                } else {
                    _state.update { state ->
                        state.copy(
                            alertDialogState = AddAlarmAlertDialogTextState(
                                if (event.dateInMillis == null)
                                    "Выберите день!"
                                else
                                    "Нельзя поставить будильник в прошлое!",
                                if (event.dateInMillis == null)
                                    "Выберите дни недели, на которые будет установлен ${state.alarm.name}."
                                else
                                    "${state.alarm.name} не сможет разбудить вас в прошлом."
                            )
                        )
                    }
                }
            }
        }
    }

    fun onAlarmItemEvent(event: AlarmItemEvent) {
        viewModelScope.launch {
            when (event) {
                is AlarmItemClockClickedEvent -> launchTimePickerDialog()
                is AlarmItemSetOnStateEvent -> {
                    _state.update { state ->
                        state.copy(
                            alarm = state.alarm.copy(
                                isOn = event.isOn
                            )
                        )
                    }
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

    private suspend fun insertOrUpdateAlarm(
        isNew: Boolean,
        alarmInput: AlarmData,
        daysOfWeek: MutableList<Boolean>
    ): Boolean =
        withContext(Dispatchers.IO) {

            insertOrUpdateAlarmToDb(isNew, alarmInput, daysOfWeek)
            return@withContext true
        }

    private suspend fun insertOrUpdateAlarmToDb(
        isNew: Boolean,
        alarmInput: AlarmData,
        daysOfWeek: MutableList<Boolean>
    ) = withContext(Dispatchers.IO) {
        if (alarmInput.name == "") alarmInput.name = "Будильник"
        if (alarmInput.activateDate != null) {
            if (isNew) {
                alarmInput.id = alarmDbRepository.insertAlarmToDb(alarmInput)
                if (alarmInput.isOn)
                    alarmInput.let(creator::create)
            } else {
                alarmDbRepository.updateAlarmInDbWithGames(alarmInput)
                if (alarmInput.isOn)
                    alarmInput.let(creator::update)
                else
                    alarmInput.let(creator::cancel)
            }
        }

        if (!isNew) {
            val day = daysOfWeek.indexOfFirst { it }
            daysOfWeek[day] = false
            val alarm = alarmInput.copy(
                dayOfWeek = day
            )
            alarmDbRepository.updateAlarmInDbWithGames(alarm)
            if (alarm.isOn)
                alarm.let(creator::update)
            else
                alarm.let(creator::cancel)
        }
        daysOfWeek.forEachIndexed { index, value ->
            if (value) {
                val alarm = alarmInput.copy(
                    dayOfWeek = index
                )
                alarm.id = alarmDbRepository.insertAlarmToDb(alarm)
                if (alarm.isOn)
                    alarm.let(creator::create)
            }
        }
        alarmDbRepository.updateAlarmInDbWithGames(alarmInput)
        if (alarmInput.isOn)
            alarmInput.let(creator::update)
        else
            alarmInput.let(creator::cancel)
    }

    suspend fun getAlarm(id: Long): AlarmData = withContext(Dispatchers.IO) {
        return@withContext alarmDbRepository.getAlarmWithGames(id)
    }

    fun setAlarm(isNew: Boolean = true, alarm: AlarmData) {
        _state.update {
            getDefaultState().copy(
                isNew = isNew,
                alarm = alarm,
                daysOfWeek = MutableList(7) { i ->
                    i == alarm.dayOfWeek
                }
            )
        }
    }

    fun setGamesList(gamesList: List<GameData>) {
        _state.update { state ->
            state.copy(
                alarm = state.alarm.copy(
                    gamesList = ArrayList(gamesList.map { it.id })
                ),
                selectedGamesList = gamesList
            )
        }
    }
}
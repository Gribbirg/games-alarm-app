package com.example.smartalarm.ui.compose.addalarm

import com.example.smartalarm.data.data.AlarmData

abstract class AddAlarmEvent

class AddAlarmTimeClickedEvent : AddAlarmEvent()

data class AddAlarmDayOfWeekSelectedEvent(
    val dayOfWeek: Int,
    val isOn: Boolean
) : AddAlarmEvent()


data class AddAlarmNameChangeEvent(
    val name: String
) : AddAlarmEvent()

data class AddAlarmVibrationChangeEvent(
    val isVibration: Boolean
) : AddAlarmEvent()


data class AddAlarmRisingVolumeChangeEvent(
    val isRisingVolume: Boolean
) : AddAlarmEvent()


class AddAlarmSaveEvent : AddAlarmEvent()


class AddAlarmAlertDialogCloseEvent : AddAlarmEvent()

data class AddAlarmRingtoneSelectedEvent(
    val ringtonePath: String
) : AddAlarmEvent()
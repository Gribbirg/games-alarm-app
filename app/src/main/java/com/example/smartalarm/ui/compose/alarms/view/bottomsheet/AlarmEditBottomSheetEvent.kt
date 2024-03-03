package com.example.smartalarm.ui.compose.alarms.view.bottomsheet

import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.ui.compose.alarms.AlarmsEvent

open class AlarmEditBottomSheetEvent : AlarmsEvent()

class AlarmEditBottomSheetCloseEvent : AlarmEditBottomSheetEvent()

class AlarmEditBottomSheetOnDeleteClickedEvent(val alarm: AlarmData) : AlarmEditBottomSheetEvent()

class AlarmEditBottomSheetOnEditClickedEvent(val alarm: AlarmData) : AlarmEditBottomSheetEvent()

class AlarmEditBottomSheetOnCopyClickedEvent(val alarm: AlarmData) : AlarmEditBottomSheetEvent()
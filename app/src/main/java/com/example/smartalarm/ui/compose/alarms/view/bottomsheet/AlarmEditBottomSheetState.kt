package com.example.smartalarm.ui.compose.alarms.view.bottomsheet

import com.example.smartalarm.data.data.AlarmData

open class AlarmEditBottomSheetState

class AlarmEditBottomSheetOnState(
    val alarm: AlarmData
) : AlarmEditBottomSheetState()


class AlarmEditBottomSheetOffState : AlarmEditBottomSheetState()
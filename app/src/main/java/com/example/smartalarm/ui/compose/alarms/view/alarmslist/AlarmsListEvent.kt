package com.example.smartalarm.ui.compose.alarms.view.alarmslist

import com.example.smartalarm.ui.compose.alarms.AlarmsEvent

open class AlarmsListEvent : AlarmsEvent()

data class AlarmsListPagerScrollEvent(val dayNum: Int) : AlarmsListEvent()
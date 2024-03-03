package com.example.smartalarm.ui.compose.alarms.view.alarmslist

import com.example.smartalarm.data.data.AlarmData

abstract class AlarmsListState(
    val dayNum: Int,
) {
    abstract fun copy(dayNum: Int = this.dayNum): AlarmsListState
}

class AlarmsListLoadingState(dayNum: Int) : AlarmsListState(dayNum) {
    override fun copy(dayNum: Int) = AlarmsListLoadingState(dayNum)
}

class AlarmsListLoadedState(
    dayNum: Int,
    val alarmsList: List<List<AlarmData>>
) : AlarmsListState(dayNum) {
    override fun copy(dayNum: Int) = AlarmsListLoadedState(dayNum, alarmsList)
}

class AlarmsListErrorState(
    dayNum: Int,
    val text: String
) : AlarmsListState(dayNum) {
    override fun copy(dayNum: Int): AlarmsListState = AlarmsListErrorState(dayNum, text)
}
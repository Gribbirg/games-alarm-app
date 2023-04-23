package com.example.smartalarm.data

class AlarmData(_hour : Int, _minute : Int, _dayOfWeek: Int, _name : String = "Будильник") {
    var time = TimeData(_hour, _minute)
    var dayOfWeek = _dayOfWeek
    var name = _name
    var record : TimeData? = null
    var isOn = false

    fun getTimeString() = time.toString()

    fun getRecordString() =
        if (record == null)
            "Нет рекорда"
        else
            "Лучшее время: ${record.toString()}"
}
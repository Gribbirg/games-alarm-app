package com.example.smartalarm.data

class TimeData(_hour: Int, _minute: Int) {
    var hour = _hour
    var minute = _minute

    override fun toString() = "${hour}:${minute}"
}
package com.example.smartalarm.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AlarmSimpleData::class], version = 1)
abstract class AlarmsDB : RoomDatabase() {

    abstract fun alarmsDao() : AlarmsDao
}
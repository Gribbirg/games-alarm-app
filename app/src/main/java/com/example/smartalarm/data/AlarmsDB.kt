package com.example.smartalarm.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AlarmSimpleData::class], version = 1)
abstract class AlarmsDB : RoomDatabase() {

    abstract fun alarmsDao() : AlarmsDao

    companion object {

        @Volatile
        private var instance : AlarmsDB? = null

        fun getInstance(context: Context): AlarmsDB {
            synchronized(this) {
                var instance = this.instance
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AlarmsDB::class.java,
                        "alarms_db"
                    ).build()
                    this.instance = instance
                }
                return instance
            }
        }
    }
}
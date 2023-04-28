package com.example.smartalarm.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AlarmSimpleData::class,
        AlarmInfoData::class,
        AlarmGamesData::class
    ], version = 4
)
abstract class AlarmsDB : RoomDatabase() {

    abstract fun alarmsDao(): AlarmsDao

    companion object {

        @Volatile
        private var INSTANCE: AlarmsDB? = null

        fun getInstance(context: Context): AlarmsDB? {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AlarmsDB::class.java,
                    "AlarmsDb"
                )
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return INSTANCE
        }
    }
}
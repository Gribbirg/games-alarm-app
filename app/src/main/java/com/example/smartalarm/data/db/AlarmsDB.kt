package com.example.smartalarm.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import java.util.concurrent.Executors

@Database(
    entities = [
        AlarmSimpleData::class,
        AlarmInfoData::class,
        AlarmUserGamesData::class,
        GameData::class
    ],
    version = 11
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
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Executors.newSingleThreadExecutor().execute {
                                getInstance(context)?.alarmsDao()?.getAlarms()
                                getInstance(context)?.alarmsDao()?.insertGamesData(ALL_GAMES)
                            }
                        }
                    })
                    .build()
            }
            return INSTANCE
        }
    }
}
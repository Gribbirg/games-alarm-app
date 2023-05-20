package com.example.smartalarm.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.smartalarm.data.repositories.AlarmDbRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.supervisorScope
import java.util.concurrent.Executors

@Database(
    entities = [
        AlarmSimpleData::class,
        AlarmInfoData::class,
        AlarmUserGamesData::class,
        GameData::class,
        RecordsData::class
    ],
    version = 14
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
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Executors.newSingleThreadExecutor().execute {

                                val dao = getInstance(context)?.alarmsDao()!!
                                val games = dao.getAlarms()
                                if (games.size != ALL_GAMES.size){
                                    dao.deleteAllUserGames()
                                    dao.deleteAllRecords()
                                    dao.deleteAllAlarms()
                                    dao.deleteAllGames()
                                }

                                dao.insertGamesData(ALL_GAMES)
                            }
                        }
                    })
                    .build()
            }
            return INSTANCE
        }
    }
}
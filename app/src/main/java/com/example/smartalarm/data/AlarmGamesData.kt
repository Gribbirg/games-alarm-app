package com.example.smartalarm.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "games_table",
    indices = [androidx.room.Index("id")],
    foreignKeys = [
        ForeignKey(
            entity = AlarmSimpleData::class,
            parentColumns = ["id"],
            childColumns = ["alarm_id"]
        )
    ]
)
data class AlarmGamesData (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id : Long = 0L,

    @ColumnInfo(name = "game_id")
    var idGame: Int,

    @ColumnInfo(name = "alarm_id")
    var idAlarm: Int,

    ) {}
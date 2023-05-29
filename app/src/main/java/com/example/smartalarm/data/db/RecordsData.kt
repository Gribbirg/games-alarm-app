package com.example.smartalarm.data.db

import androidx.core.text.isDigitsOnly
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "records_table",
    indices = [androidx.room.Index("id")],
    foreignKeys = [
        ForeignKey(
            entity = GameData::class,
            parentColumns = ["id"],
            childColumns = ["game_id"]
        )
    ]
)
data class RecordsData(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0L,

    @ColumnInfo(name = "game_id")
    var gameId: Int,

    @ColumnInfo(name = "game_name")
    var gameName: String,

    @ColumnInfo(name = "date")
    var date: String? = null,

    @ColumnInfo(name = "record_score")
    var recordScore: Int? = null,

    @ColumnInfo(name = "record_time")
    var recordTime: String? = null,
) {
    constructor(gameData: GameData): this (gameId = gameData.id, gameName = gameData.name)
}
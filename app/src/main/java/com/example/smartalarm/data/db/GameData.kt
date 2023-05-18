package com.example.smartalarm.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games_table")
data class GameData(
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: Int,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "record")
    var record: Int? = null,

    @ColumnInfo(name = "record_time")
    var recordTime: String? = null,

    @ColumnInfo(name = "record_date")
    var recordDate: String? = null
) {
    constructor(record: RecordsData) : this(
        id = 0,
        name = record.gameName,
        record = record.recordScore,
        recordTime = record.recordTime,
        recordDate = record.date
    )
}
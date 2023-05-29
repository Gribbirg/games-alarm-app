package com.example.smartalarm.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games_table")
data class GameData(
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: Int = 0,

    @ColumnInfo(name = "name")
    var name: String = "Игруха",

    @ColumnInfo(name = "record")
    var record: Int? = null,

    @ColumnInfo(name = "record_time")
    var recordTime: String? = null,

    @ColumnInfo(name = "record_date")
    var recordDate: String? = null,

    @ColumnInfo(name = "record_shared")
    var recordShared: Boolean = false
) {
    constructor(record: RecordsData) : this(
        id = record.gameId,
        name = record.gameName,
        record = record.recordScore,
        recordTime = record.recordTime,
        recordDate = record.date
    )

    constructor(str: String) : this() {
        with(str.split(';')) {
            id = this[0].toInt()
            name = this[1]
            record = if (this[2] == "null") null else this[2].toInt()
            recordTime = if (this[3] == "null") null else this[3]
            recordDate = if (this[4] == "null") null else this[4]
        }
    }

    override fun toString(): String {
        return "$id;$name;$record;$recordTime;$recordDate"
    }

    fun toStringList(current: String): String {
        val list = getRecordsList(current)
        if (list[id - 1] == null)
            list[id - 1] = this
        else if (list[id - 1]?.record!! < record!!) {
            list[id - 1] = this
        }
        return arrayToString(list)
    }
}

fun arrayToString(list: ArrayList<GameData?>): String {
    var res = ""
    for (i in list) {
        res += i?.toString() ?: "null"
        res += '/'
    }
    return res.substring(0, res.length - 1)
}
fun getRecordsList(records: String): ArrayList<GameData?> {
    val res = ArrayList<GameData?>()
    for (i in records.split('/')) {
        res.add(
            if (i == "null") null else GameData(i)
        )
    }
    return res
}
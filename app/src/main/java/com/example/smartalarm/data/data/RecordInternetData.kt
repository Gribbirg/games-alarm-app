package com.example.smartalarm.data.data

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.example.smartalarm.data.db.GameData
import com.example.smartalarm.data.db.RecordsData

data class RecordInternetData(
    var id: Long = 0L,
    var gameId: Int = 0,
    var name: String = "Игруха",
    var record: Int? = null,
    var recordTime: String? = null,
    var recordDate: String? = null
) {
    constructor(str: String) : this() {
        with(str.split(';')) {
            id = this[0].toLong()
            gameId = this[1].toInt()
            name = this[2]
            record = if (this[3] == "null") null else this[3].toInt()
            recordTime = if (this[4] == "null") null else this[4]
            recordDate = if (this[5] == "null") null else this[5]
        }
    }

    constructor(record: RecordsData) : this(
        id = record.id,
        gameId = record.gameId,
        name = record.gameName,
        record = record.recordScore,
        recordTime = record.recordTime,
        recordDate = record.date
    )

    override fun toString(): String {
        return "$id;$gameId;$name;$record;$recordTime;$recordDate"
    }
}

fun arrayToString(list: ArrayList<RecordInternetData?>): String {
    var res = ""
    if (list.size == 0) return "null"
    for (i in list) {
        res += i?.toString() ?: "null"
        res += '/'
    }
    return res.substring(0, res.length - 1)
}

fun getRecordsList(records: String): ArrayList<RecordInternetData?> {
    val res = ArrayList<RecordInternetData?>()
    for (i in records.split('/')) {
        res.add(
            if (i == "null") null else RecordInternetData(i)
        )
    }
    return res
}

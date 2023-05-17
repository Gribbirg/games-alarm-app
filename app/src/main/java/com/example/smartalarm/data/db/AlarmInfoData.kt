package com.example.smartalarm.data.db

import androidx.room.*

@Entity(
    tableName = "user_info_table",
    indices = [Index("id")],
    foreignKeys = [
        ForeignKey(
            entity = AlarmSimpleData::class,
            parentColumns = ["id"],
            childColumns = ["alarm_id"]
        )
    ]
)
data class AlarmInfoData(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id : Long = 0L,

    @ColumnInfo(name = "info_id")
    var idInfo: Int,

    @ColumnInfo(name = "alarm_id")
    var idAlarm: Int,
) {}
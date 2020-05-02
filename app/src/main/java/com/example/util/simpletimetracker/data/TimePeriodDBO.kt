package com.example.util.simpletimetracker.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timePeriods")
data class TimePeriodDBO(
    @PrimaryKey(autoGenerate = true)
    var id: Long,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "time_started")
    var timeStarted: Long,

    @ColumnInfo(name = "time_ended")
    var timeEnded: Long
)
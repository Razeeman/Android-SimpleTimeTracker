package com.example.util.simpletimetracker.data_local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "runningRecords")
data class RunningRecordDBO(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "time_started")
    val timeStarted: Long,

    @ColumnInfo(name = "comment")
    val comment: String,

    @Deprecated("storing tag ids moved to a separate database")
    @ColumnInfo(name = "tag_id")
    val tagId: Long
)
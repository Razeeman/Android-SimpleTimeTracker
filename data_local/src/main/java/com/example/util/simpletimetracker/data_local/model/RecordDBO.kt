package com.example.util.simpletimetracker.data_local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class RecordDBO(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "type_id")
    val typeId: Long,

    @ColumnInfo(name = "time_started")
    val timeStarted: Long,

    @ColumnInfo(name = "time_ended")
    val timeEnded: Long,

    @ColumnInfo(name = "comment")
    val comment: String,

    @Deprecated("storing tag ids moved to a separate database")
    @ColumnInfo(name = "tag_id")
    val tagId: Long
)
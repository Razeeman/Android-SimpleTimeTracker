package com.example.util.simpletimetracker.data_local.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "recordToRecordTag", primaryKeys = ["record_id", "record_tag_id"])
data class RecordToRecordTagDBO(
    @ColumnInfo(name = "record_id")
    val recordId: Long,
    @ColumnInfo(name = "record_tag_id")
    val recordTagId: Long
)
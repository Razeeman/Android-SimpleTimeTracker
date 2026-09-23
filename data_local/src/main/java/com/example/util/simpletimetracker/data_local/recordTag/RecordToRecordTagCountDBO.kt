package com.example.util.simpletimetracker.data_local.recordTag

import androidx.room.ColumnInfo

data class RecordToRecordTagCountDBO(
    @ColumnInfo(name = "record_tag_id")
    val recordTagId: Long,

    @ColumnInfo(name = "record_count")
    val recordCount: Int,
)

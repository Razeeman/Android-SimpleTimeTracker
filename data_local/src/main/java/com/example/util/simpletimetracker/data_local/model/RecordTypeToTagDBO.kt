package com.example.util.simpletimetracker.data_local.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "recordTypeToTag", primaryKeys = ["record_type_id", "record_tag_id"])
data class RecordTypeToTagDBO(
    @ColumnInfo(name = "record_type_id")
    val recordTypeId: Long,
    @ColumnInfo(name = "record_tag_id")
    val tagId: Long,
)
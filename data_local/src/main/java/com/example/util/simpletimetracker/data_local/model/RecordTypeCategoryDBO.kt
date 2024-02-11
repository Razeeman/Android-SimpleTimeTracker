package com.example.util.simpletimetracker.data_local.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "recordTypeCategory", primaryKeys = ["record_type_id", "category_id"])
data class RecordTypeCategoryDBO(
    @ColumnInfo(name = "record_type_id")
    val recordTypeId: Long,
    @ColumnInfo(name = "category_id")
    val categoryId: Long,
)
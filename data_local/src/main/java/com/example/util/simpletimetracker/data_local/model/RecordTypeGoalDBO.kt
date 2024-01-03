package com.example.util.simpletimetracker.data_local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordTypeGoals")
data class RecordTypeGoalDBO(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "type_id")
    val typeId: Long,

    // 0 - session
    // 1 - daily
    // 2 - weekly
    // 3 - monthly
    @ColumnInfo(name = "range")
    val range: Long,

    // 0 - goal for records duration
    // 1 - goal for records count
    @ColumnInfo(name = "type")
    val type: Long,

    // seconds if goal time
    // count if goal count
    @ColumnInfo(name = "value")
    val value: Long,

    // Only one of typeId or categoryId should be present, other should be 0.
    @ColumnInfo(name = "category_id")
    val categoryId: Long,

    // Stored as "0000000" string, where each number is a day,
    // 0 - not selected, 1 - selected,
    // starting from sunday.
    // For example, "1111100" - only work days selected.
    @ColumnInfo(name = "days_of_week")
    val daysOfWeek: String,
)
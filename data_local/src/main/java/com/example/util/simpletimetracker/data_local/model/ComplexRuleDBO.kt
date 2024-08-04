package com.example.util.simpletimetracker.data_local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "complexRules")
data class ComplexRuleDBO(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "disabled")
    val disabled: Boolean,

    @ColumnInfo(name = "actionType")
    val action: Long,

    // Longs stored in string comma separated
    @ColumnInfo(name = "actionSetTagIds")
    val actionSetTagIds: String,

    // Longs stored in string comma separated
    @ColumnInfo(name = "conditionStartingTypeIds")
    val conditionStartingTypeIds: String,

    // Longs stored in string comma separated
    @ColumnInfo(name = "conditionCurrentTypeIds")
    val conditionCurrentTypeIds: String,

    // Stored as "0000000" string, where each number is a day,
    // 0 - not selected, 1 - selected,
    // starting from sunday.
    // For example, "0111110" - only work days selected.
    @ColumnInfo(name = "conditionDaysOfWeek")
    val conditionDaysOfWeek: String,
)
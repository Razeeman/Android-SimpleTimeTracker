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

    // Days of week stored in string comma separated
    @ColumnInfo(name = "conditionDaysOfWeek")
    val conditionDaysOfWeek: String,
)
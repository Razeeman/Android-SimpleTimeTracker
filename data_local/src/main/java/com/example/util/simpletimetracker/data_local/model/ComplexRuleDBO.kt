package com.example.util.simpletimetracker.data_local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// TODO RULES rename actionSetTags to actionSetTagIds etc.
@Entity(tableName = "complexRules")
data class ComplexRuleDBO(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "actionType")
    val actionType: Long,

    // Longs stored in string comma separated
    @ColumnInfo(name = "actionSetTags")
    val actionSetTags: String,

    // Longs stored in string comma separated
    @ColumnInfo(name = "conditionStartingActivity")
    val conditionStartingActivity: String,

    // Longs stored in string comma separated
    @ColumnInfo(name = "conditionCurrentActivity")
    val conditionCurrentActivity: String,

    // Days of week stored in string comma separated
    @ColumnInfo(name = "conditionDaysOfWeek")
    val conditionDaysOfWeek: String,
)
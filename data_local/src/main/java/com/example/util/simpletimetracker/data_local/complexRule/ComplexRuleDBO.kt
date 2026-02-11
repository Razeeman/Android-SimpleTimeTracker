package com.example.util.simpletimetracker.data_local.complexRule

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.util.simpletimetracker.data_local.recordType.RecordTypeDBO

@Entity(tableName = "complexRules")
data class ComplexRuleDBO(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "disabled")
    val disabled: Boolean,

    @ColumnInfo(name = "actionType")
    val action: Long,

    @ColumnInfo(name = "actionDisallowOnlyPrevious")
    val actionDisallowOnlyPrevious: Boolean,

    // Longs stored in string comma separated
    @ColumnInfo(name = "actionSetTagIds")
    val actionSetTagIds: String,

    // Longs stored in string comma separated
    @ColumnInfo(name = "conditionStartingTypeIds")
    val conditionStartingTypeIds: String,

    // Longs stored in string comma separated
    @ColumnInfo(name = "conditionCurrentTypeIds")
    val conditionCurrentTypeIds: String,

    /**
     * How data is stored - see [RecordTypeDBO].
     */
    @ColumnInfo(name = "conditionDaysOfWeek")
    val conditionDaysOfWeek: String,
)
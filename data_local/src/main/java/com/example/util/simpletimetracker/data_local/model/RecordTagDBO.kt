package com.example.util.simpletimetracker.data_local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordTags")
data class RecordTagDBO(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "type_id")
    val typeId: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color")
    val color: Int,

    // If not empty - take color from here, custom colorInt stored as text.
    @ColumnInfo(name = "color_int")
    val colorInt: String,

    @ColumnInfo(name = "archived")
    val archived: Boolean
)
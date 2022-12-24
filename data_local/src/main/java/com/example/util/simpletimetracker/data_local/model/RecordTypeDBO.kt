package com.example.util.simpletimetracker.data_local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordTypes")
data class RecordTypeDBO(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "icon")
    val icon: String,

    @ColumnInfo(name = "color")
    val color: Int,

    // If not empty - take color from here, custom colorInt stored as text.
    @ColumnInfo(name = "color_int")
    val colorInt: String,

    @ColumnInfo(name = "hidden")
    val hidden: Boolean,

    // Seconds.
    @ColumnInfo(name = "goal_time")
    val goalTime: Long,

    // Seconds.
    @ColumnInfo(name = "daily_goal_time")
    val dailyGoalTime: Long,

    // Seconds.
    @ColumnInfo(name = "weekly_goal_time")
    val weeklyGoalTime: Long,
)
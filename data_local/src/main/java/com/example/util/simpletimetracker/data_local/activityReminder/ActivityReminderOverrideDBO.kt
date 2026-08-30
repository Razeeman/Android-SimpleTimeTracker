package com.example.util.simpletimetracker.data_local.activityReminder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activityReminderOverrides")
data class ActivityReminderOverrideDBO(
    @PrimaryKey
    @ColumnInfo(name = "activity_id")
    val activityId: Long,

    @ColumnInfo(name = "mode")
    val mode: Int,
)

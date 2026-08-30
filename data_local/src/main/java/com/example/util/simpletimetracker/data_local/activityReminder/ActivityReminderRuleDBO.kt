package com.example.util.simpletimetracker.data_local.activityReminder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.util.simpletimetracker.data_local.recordType.RecordTypeGoalDBO

@Entity(
    tableName = "activityReminderRules",
    foreignKeys = [
        ForeignKey(
            entity = ActivityReminderOverrideDBO::class,
            parentColumns = ["activity_id"],
            childColumns = ["activity_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["activity_id"])],
)
data class ActivityReminderRuleDBO(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "activity_id")
    val activityId: Long,

    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Long,

    @ColumnInfo(name = "recurrent")
    val recurrent: Boolean,

    /**
     * How data is stored - see [RecordTypeGoalDBO].
     */
    @ColumnInfo(name = "weekdays")
    val weekdays: String,

    @ColumnInfo(name = "dnd_start_millis")
    val doNotDisturbStartMillis: Long,

    @ColumnInfo(name = "dnd_end_millis")
    val doNotDisturbEndMillis: Long,
)

package com.example.util.simpletimetracker.data_local.scheduledReminder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.util.simpletimetracker.data_local.recordType.RecordTypeGoalDBO

@Entity(tableName = "scheduledReminders")
data class ScheduledReminderDBO(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "enabled")
    val enabled: Boolean,

    @ColumnInfo(name = "text")
    val text: String,

    // weekly 0
    // ome time 1
    // monthly 2
    // hourly 3
    // activity started 4
    // activity stopped 5
    @ColumnInfo(name = "schedule_type")
    val scheduleType: Int,

    @ColumnInfo(name = "time_of_day_millis")
    val timeOfDayMillis: Long,

    /**
     * How data is stored - see [RecordTypeGoalDBO].
     */
    @ColumnInfo(name = "weekdays")
    val weekdays: String?,

    @ColumnInfo(name = "one_time_local_epoch_day")
    val date: Long?,

    @ColumnInfo(name = "monthly_day_of_month")
    val monthlyDayOfMonth: Int?,

    @ColumnInfo(name = "interval_seconds")
    val intervalSeconds: Long?,

    @ColumnInfo(name = "dnd_start_millis")
    val doNotDisturbStartMillis: Long?,

    @ColumnInfo(name = "dnd_end_millis")
    val doNotDisturbEndMillis: Long?,

    // always 0
    // records not tracked 1
    @ColumnInfo(name = "condition_type")
    val conditionType: Int,

    @ColumnInfo(name = "target_id")
    val targetId: Long?,

    // activity 0
    // category 1
    // tag 2
    @ColumnInfo(name = "target_type")
    val targetType: Int,
)

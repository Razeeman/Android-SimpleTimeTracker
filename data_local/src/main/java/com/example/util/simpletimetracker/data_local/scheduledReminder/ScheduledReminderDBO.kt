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
    val oneTimeDate: Long?,

    @ColumnInfo(name = "monthly_day_of_month")
    val monthlyDayOfMonth: Int?,

    // always 0
    // activity mot tracked 1
    @ColumnInfo(name = "condition_type")
    val conditionType: Int,

    @ColumnInfo(name = "activity_id")
    val activityId: Long?,
)

package com.example.util.simpletimetracker.data_local.activityReminder

import androidx.room.Embedded
import androidx.room.Relation

data class ActivityReminderOverrideWithRulesDBO(
    @Embedded
    val override: ActivityReminderOverrideDBO,

    @Relation(
        entity = ActivityReminderRuleDBO::class,
        parentColumn = "activity_id",
        entityColumn = "activity_id",
    )
    val rules: List<ActivityReminderRuleDBO>,
)

package com.example.util.simpletimetracker.domain.activityReminder.repo

import com.example.util.simpletimetracker.domain.activityReminder.model.ActivityReminderOverride

interface ActivityReminderOverrideRepo {

    suspend fun getAll(): List<ActivityReminderOverride>

    suspend fun get(activityId: Long): ActivityReminderOverride?

    suspend fun save(data: ActivityReminderOverride)

    suspend fun restoreFromBackup(data: List<ActivityReminderOverride>)

    suspend fun remove(activityId: Long)

    suspend fun clear()
}

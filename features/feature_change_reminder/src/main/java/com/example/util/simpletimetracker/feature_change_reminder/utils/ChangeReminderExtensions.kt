package com.example.util.simpletimetracker.feature_change_reminder.utils

import com.example.util.simpletimetracker.feature_change_reminder.model.ChangeReminderEditor
import com.example.util.simpletimetracker.feature_change_reminder.model.ChangeReminderEditor.ScheduleType.ACTIVITY_STARTED
import com.example.util.simpletimetracker.feature_change_reminder.model.ChangeReminderEditor.ScheduleType.ACTIVITY_STOPPED

fun ChangeReminderEditor.ScheduleType.isActivityEvent(): Boolean {
    return this == ACTIVITY_STARTED || this == ACTIVITY_STOPPED
}
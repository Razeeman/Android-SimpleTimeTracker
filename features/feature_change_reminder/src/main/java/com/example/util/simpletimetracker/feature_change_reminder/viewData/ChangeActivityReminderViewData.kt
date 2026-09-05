package com.example.util.simpletimetracker.feature_change_reminder.viewData

import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner

data class ChangeActivityReminderViewData(
    val activityName: String,
    val activitySelectionEnabled: Boolean,
    val modeItems: List<CustomSpinner.CustomSpinnerItem>,
    val modeSelectedPosition: Int,
    val customFieldsVisible: Boolean,
    val durationText: String,
    val recurrent: Boolean,
    val daysOfWeek: List<DayOfWeekViewData>,
    val doNotDisturbStartText: String,
    val doNotDisturbEndText: String,
    val controlsEnabled: Boolean,
    val deleteVisible: Boolean,
)

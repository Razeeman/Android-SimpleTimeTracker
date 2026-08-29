package com.example.util.simpletimetracker.feature_change_reminder.viewData

import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_change_reminder.model.ChangeReminderEditor
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner

data class ChangeReminderViewData(
    val message: String,
    val scheduleType: ChangeReminderEditor.ScheduleType,
    val scheduleItems: List<CustomSpinner.CustomSpinnerItem>,
    val scheduleSelectedPosition: Int,
    val conditionItems: List<CustomSpinner.CustomSpinnerItem>,
    val conditionSelectedPosition: Int,
    val daysOfWeek: List<DayOfWeekViewData>,
    val dateText: String,
    val dayOfMonthItems: List<CustomSpinner.CustomSpinnerItem>,
    val dayOfMonthSelectedPosition: Int,
    val timeText: String,
    val conditionType: ChangeReminderEditor.ConditionType,
    val activityName: String,
    val deleteVisible: Boolean,
    val controlsEnabled: Boolean,
)

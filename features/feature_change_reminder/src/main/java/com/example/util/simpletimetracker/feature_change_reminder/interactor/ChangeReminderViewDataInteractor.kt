package com.example.util.simpletimetracker.feature_change_reminder.interactor

import com.example.util.simpletimetracker.core.mapper.ChangeReminderViewDataMapper
import com.example.util.simpletimetracker.core.mapper.DayOfWeekViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.extension.toLocalDateTime
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.domain.utils.LocalDateMapper
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_change_reminder.R
import com.example.util.simpletimetracker.feature_change_reminder.model.ChangeReminderEditor
import com.example.util.simpletimetracker.feature_change_reminder.model.ChangeReminderEditor.ConditionType
import com.example.util.simpletimetracker.feature_change_reminder.model.ChangeReminderEditor.ScheduleType
import com.example.util.simpletimetracker.feature_change_reminder.utils.isActivityEvent
import com.example.util.simpletimetracker.feature_change_reminder.viewData.ChangeReminderViewData
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import java.util.TimeZone
import javax.inject.Inject

class ChangeReminderViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val dayOfWeekViewDataMapper: DayOfWeekViewDataMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
    private val localDateMapper: LocalDateMapper,
    private val changeReminderViewDataMapper: ChangeReminderViewDataMapper,
) {

    private val daysOfMonth = (1..ChangeReminderEditor.DAYS_IN_MONTH).toList()

    private val scheduleTypes = listOf(
        ScheduleType.ONE_TIME,
        ScheduleType.HOURLY,
        ScheduleType.WEEKLY,
        ScheduleType.MONTHLY,
        ScheduleType.ACTIVITY_STARTED,
        ScheduleType.ACTIVITY_STOPPED,
    )

    private val conditionTypes = listOf(
        ConditionType.ALWAYS,
        ConditionType.NOT_TRACKED,
    )

    suspend fun getViewData(
        editor: ChangeReminderEditor,
        selectedTargetName: String?,
        controlsEnabled: Boolean,
    ): ChangeReminderViewData {
        val timeZone = TimeZone.getDefault()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val currentTimestamp = currentTimestampProvider.get()
        val dateTimestamp = localDateMapper.resolveDateTime(
            dateEpochDay = editor.date,
            timeOfDayMillis = editor.timeOfDayMillis,
            timeZone = timeZone,
        ) ?: currentTimestamp
        val today = currentTimestamp.toLocalDateTime(timeZone).toLocalDate()
        val timeTimestamp = localDateMapper.resolveDateTime(
            date = today,
            timeOfDayMillis = editor.timeOfDayMillis,
            timeZone = timeZone,
        ) ?: currentTimestamp

        return ChangeReminderViewData(
            message = editor.message,
            scheduleType = editor.scheduleType,
            scheduleItems = mapScheduleItems(),
            scheduleSelectedPosition = scheduleTypes.indexOf(editor.scheduleType),
            daysOfWeek = mapDaysItems(editor),
            conditionItems = mapConditionItems(),
            conditionSelectedPosition = conditionTypes.indexOf(editor.conditionType),
            conditionText = mapConditionText(editor, selectedTargetName),
            dateText = timeMapper.formatDateYear(dateTimestamp),
            dayOfMonthItems = mapDayOfMonthItems(),
            dayOfMonthSelectedPosition = daysOfMonth.indexOf(editor.dayOfMonth),
            timeText = timeMapper.formatTime(
                time = timeTimestamp,
                useMilitaryTime = useMilitaryTime,
                showSeconds = false,
            ),
            intervalText = timeMapper.formatDuration(editor.intervalSeconds),
            doNotDisturbStartText = changeReminderViewDataMapper.formatTimeOfDay(
                millis = editor.doNotDisturbStartMillis,
                useMilitaryTime = useMilitaryTime,
                date = today,
                timeZone = timeZone,
            ),
            doNotDisturbEndText = changeReminderViewDataMapper.formatTimeOfDay(
                millis = editor.doNotDisturbEndMillis,
                useMilitaryTime = useMilitaryTime,
                date = today,
                timeZone = timeZone,
            ),
            conditionType = editor.conditionType,
            deleteVisible = editor.id != 0L,
            controlsEnabled = controlsEnabled,
        )
    }

    fun mapDayOfMonth(position: Int): Int? {
        return daysOfMonth.getOrNull(position)
    }

    fun mapSchedule(position: Int): ScheduleType? {
        return scheduleTypes.getOrNull(position)
    }

    fun mapCondition(position: Int): ConditionType? {
        return conditionTypes.getOrNull(position)
    }

    private fun mapScheduleItems(): List<CustomSpinner.CustomSpinnerTextItem> {
        return scheduleTypes.map {
            val textRes = when (it) {
                ScheduleType.WEEKLY -> R.string.reminders_schedule_weekly
                ScheduleType.ONE_TIME -> R.string.reminders_schedule_one_time
                ScheduleType.MONTHLY -> R.string.reminders_schedule_monthly
                ScheduleType.HOURLY -> R.string.reminders_schedule_hourly
                ScheduleType.ACTIVITY_STARTED -> R.string.reminders_schedule_record_started
                ScheduleType.ACTIVITY_STOPPED -> R.string.reminders_schedule_record_stopped
            }
            CustomSpinner.CustomSpinnerTextItem(resourceRepo.getString(textRes))
        }
    }

    private fun mapConditionItems(): List<CustomSpinner.CustomSpinnerTextItem> {
        return conditionTypes.map {
            val textRes = when (it) {
                ConditionType.ALWAYS -> R.string.change_reminder_condition_always
                ConditionType.NOT_TRACKED -> R.string.reminders_condition_records_not_tracked
            }
            CustomSpinner.CustomSpinnerTextItem(resourceRepo.getString(textRes))
        }
    }

    private fun mapConditionText(
        editor: ChangeReminderEditor,
        selectedTargetName: String?,
    ): String {
        if (editor.scheduleType.isActivityEvent() && selectedTargetName == null) {
            return resourceRepo.getString(R.string.change_record_message_choose_type)
        }
        if (editor.conditionType == ConditionType.ALWAYS || selectedTargetName == null) {
            return resourceRepo.getString(R.string.change_reminder_condition_always)
        }
        val targetType = when (editor.conditionTarget) {
            is ScheduledReminder.Condition.Target.Activity -> R.string.activity_hint
            is ScheduledReminder.Condition.Target.Category -> R.string.category_hint
            is ScheduledReminder.Condition.Target.Tag -> R.string.record_tag_hint
            null -> return resourceRepo.getString(R.string.change_reminder_condition_always)
        }
        return listOf(
            resourceRepo.getString(targetType),
            selectedTargetName,
        ).joinToString(separator = " · ")
    }

    private suspend fun mapDaysItems(
        editor: ChangeReminderEditor,
    ): List<DayOfWeekViewData> {
        return dayOfWeekViewDataMapper.mapViewData(
            selectedDaysOfWeek = editor.daysOfWeek,
            isDarkTheme = prefsInteractor.getDarkMode(),
            firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
            width = DayOfWeekViewData.Width.MatchParent,
            paddingHorizontalDp = 0,
        )
    }

    private fun mapDayOfMonthItems(): List<CustomSpinner.CustomSpinnerTextItem> {
        return daysOfMonth.map {
            CustomSpinner.CustomSpinnerTextItem(it.toString())
        }
    }
}
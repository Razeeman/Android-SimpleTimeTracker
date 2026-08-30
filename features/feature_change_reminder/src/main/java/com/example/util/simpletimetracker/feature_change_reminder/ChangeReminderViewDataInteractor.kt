package com.example.util.simpletimetracker.feature_change_reminder

import com.example.util.simpletimetracker.core.mapper.DayOfWeekViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.extension.toLocalDateTime
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledReminderOccurrenceCalculator
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_change_reminder.model.ChangeReminderEditor
import com.example.util.simpletimetracker.feature_change_reminder.model.ChangeReminderEditor.ConditionType
import com.example.util.simpletimetracker.feature_change_reminder.model.ChangeReminderEditor.ScheduleType
import com.example.util.simpletimetracker.feature_change_reminder.viewData.ChangeReminderViewData
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import java.util.TimeZone
import javax.inject.Inject

class ChangeReminderViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val occurrenceCalculator: ScheduledReminderOccurrenceCalculator,
    private val dayOfWeekViewDataMapper: DayOfWeekViewDataMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
) {

    private val daysOfMonth = (1..ChangeReminderEditor.DAYS_IN_MONTH).toList()

    private val scheduleTypes = listOf(
        ScheduleType.WEEKLY,
        ScheduleType.ONE_TIME,
        ScheduleType.MONTHLY,
    )

    private val conditionTypes = listOf(
        ConditionType.ALWAYS,
        ConditionType.NOT_TRACKED,
    )

    suspend fun getViewData(
        editor: ChangeReminderEditor,
        selectedActivity: RecordType?,
        controlsEnabled: Boolean,
    ): ChangeReminderViewData {
        val timeZone = TimeZone.getDefault()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val currentTimestamp = currentTimestampProvider.get()
        val dateTimestamp = occurrenceCalculator.resolveLocalDateTime(
            dateEpochDay = editor.oneTimeDate,
            timeOfDayMillis = editor.timeOfDayMillis,
            timeZone = timeZone,
        ).takeUnless { it == 0L } ?: currentTimestamp
        val today = currentTimestamp.toLocalDateTime(timeZone).toLocalDate()
        val timeTimestamp = occurrenceCalculator.resolveLocalDateTime(
            dateEpochDay = today.toEpochDay(),
            timeOfDayMillis = editor.timeOfDayMillis,
            timeZone = timeZone,
        ).takeUnless { it == 0L } ?: currentTimestamp
        val activityName = selectedActivity?.name
            ?: resourceRepo.getString(R.string.change_record_message_choose_type)

        return ChangeReminderViewData(
            message = editor.message,
            scheduleType = editor.scheduleType,
            scheduleItems = mapScheduleItems(),
            scheduleSelectedPosition = scheduleTypes.indexOf(editor.scheduleType),
            daysOfWeek = mapDaysItems(editor),
            conditionItems = mapConditionItems(),
            conditionSelectedPosition = conditionTypes.indexOf(editor.conditionType),
            dateText = timeMapper.formatDateYear(dateTimestamp),
            dayOfMonthItems = mapDayOfMonthItems(),
            dayOfMonthSelectedPosition = daysOfMonth.indexOf(editor.dayOfMonth),
            timeText = timeMapper.formatTime(
                time = timeTimestamp,
                useMilitaryTime = useMilitaryTime,
                showSeconds = false,
            ),
            conditionType = editor.conditionType,
            activityName = activityName,
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
            }
            CustomSpinner.CustomSpinnerTextItem(resourceRepo.getString(textRes))
        }
    }

    private fun mapConditionItems(): List<CustomSpinner.CustomSpinnerTextItem> {
        return conditionTypes.map {
            val textRes = when (it) {
                ConditionType.ALWAYS -> R.string.change_reminder_condition_always
                ConditionType.NOT_TRACKED -> R.string.reminders_condition_activity_not_tracked
            }
            CustomSpinner.CustomSpinnerTextItem(resourceRepo.getString(textRes))
        }
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
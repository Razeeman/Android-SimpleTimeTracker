package com.example.util.simpletimetracker.feature_reminders.interactor

import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledReminderInteractor
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledReminderOccurrenceCalculator
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_reminders.mapper.ReminderViewDataMapper
import java.util.TimeZone
import javax.inject.Inject

class RemindersViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val scheduledReminderInteractor: ScheduledReminderInteractor,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val scheduledReminderOccurrenceCalculator: ScheduledReminderOccurrenceCalculator,
    private val reminderViewDataMapper: ReminderViewDataMapper,
) {

    suspend fun getViewData(): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val activities = recordTypeInteractor.getAll().associateBy(RecordType::id)
        val timeZone = TimeZone.getDefault()
        val reminders = getReminders(
            nowTimestamp = currentTimestampProvider.get(),
            timeZone = timeZone,
        )

        val result = mutableListOf<ViewHolderType>()

        result += reminders.map { reminder ->
            val activityId = (reminder.condition as? ScheduledReminder.Condition.ActivityNotTrackedToday)
                ?.activityId
            reminderViewDataMapper.map(
                reminder = reminder,
                activity = activityId?.let(activities::get),
                isDarkTheme = isDarkTheme,
                useMilitaryTime = useMilitaryTime,
                firstDayOfWeek = firstDayOfWeek,
                timeZone = timeZone,
            )
        }
        result += reminderViewDataMapper.mapAddItem(isDarkTheme)

        return result
    }

    private suspend fun getReminders(
        nowTimestamp: Long,
        timeZone: TimeZone,
    ): List<ScheduledReminder> {
        val comparator = compareBy(
            { if (it.enabled) 0 else 1 },
            {
                if (it.enabled) {
                    scheduledReminderOccurrenceCalculator.calculateNext(
                        schedule = it.schedule,
                        nowTimestamp = nowTimestamp,
                        timeZone = timeZone,
                        catchUpOverdueOneTime = true,
                    )?.triggerTimestamp ?: Long.MAX_VALUE
                } else {
                    Long.MAX_VALUE
                }
            },
            ScheduledReminder::id,
        )
        return scheduledReminderInteractor.getAll().sortedWith(comparator)
    }
}

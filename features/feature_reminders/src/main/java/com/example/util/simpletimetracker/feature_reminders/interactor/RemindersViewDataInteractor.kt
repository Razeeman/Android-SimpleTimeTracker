package com.example.util.simpletimetracker.feature_reminders.interactor

import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.activityReminder.model.ActivityReminderOverride
import com.example.util.simpletimetracker.domain.activityReminder.repo.ActivityReminderOverrideRepo
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledReminderInteractor
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledReminderOccurrenceCalculator
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_reminders.mapper.ReminderViewDataMapper
import com.example.util.simpletimetracker.feature_reminders.mapper.ActivityReminderViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_base_adapter.header.HeaderViewData
import com.example.util.simpletimetracker.feature_reminders.R
import com.example.util.simpletimetracker.feature_reminders.viewData.RemindersHeader
import java.util.TimeZone
import javax.inject.Inject

class RemindersViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val scheduledReminderInteractor: ScheduledReminderInteractor,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val scheduledReminderOccurrenceCalculator: ScheduledReminderOccurrenceCalculator,
    private val reminderViewDataMapper: ReminderViewDataMapper,
    private val activityReminderOverrideRepo: ActivityReminderOverrideRepo,
    private val activityReminderViewDataMapper: ActivityReminderViewDataMapper,
    private val resourceRepo: ResourceRepo,
) {

    suspend fun getViewData(): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val activityList = recordTypeInteractor.getAll()
        val activities = activityList.associateBy(RecordType::id)
        val activityReminderOverrides = activityReminderOverrideRepo.getAll()
            .associateBy { it.activityId }
        val reminders = getReminders(
            nowTimestamp = currentTimestampProvider.get(),
            timeZone = TimeZone.getDefault(),
        )

        val result = mutableListOf<ViewHolderType>()

        result += HeaderViewData(
            section = RemindersHeader.Activity,
            text = resourceRepo.getString(R.string.notification_activity_title),
        )
        result += activities.mapNotNull { (activityId, activity) ->
            val override = activityReminderOverrides[activityId] ?: return@mapNotNull null
            activityReminderViewDataMapper.map(
                activity = activity,
                override = override,
                isDarkTheme = isDarkTheme,
                useMilitaryTime = useMilitaryTime,
                firstDayOfWeek = firstDayOfWeek,
            )
        }
        result += HeaderViewData(
            section = RemindersHeader.Scheduled,
            text = resourceRepo.getString(R.string.settings_reminders_title),
        )
        result += reminders.map { reminder ->
            val activityId = (reminder.condition as? ScheduledReminder.Condition.ActivityNotTrackedToday)
                ?.activityId
            reminderViewDataMapper.map(
                reminder = reminder,
                activity = activityId?.let(activities::get),
                isDarkTheme = isDarkTheme,
                useMilitaryTime = useMilitaryTime,
                firstDayOfWeek = firstDayOfWeek,
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
                        catchUpOverdueOneTime = true,
                        timeZone = timeZone,
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

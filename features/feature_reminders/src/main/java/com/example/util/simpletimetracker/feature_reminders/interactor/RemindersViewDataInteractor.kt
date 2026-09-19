package com.example.util.simpletimetracker.feature_reminders.interactor

import com.example.util.simpletimetracker.core.mapper.RecordTagViewDataMapper
import com.example.util.simpletimetracker.domain.activityReminder.repo.ActivityReminderOverrideRepo
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.category.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.category.model.Category
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.scheduledReminder.interactor.ScheduledReminderInteractor
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_reminders.mapper.ReminderViewDataMapper
import com.example.util.simpletimetracker.feature_reminders.mapper.ActivityReminderViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.record.model.RecordTimerEvent
import com.example.util.simpletimetracker.feature_base_adapter.header.HeaderViewData
import com.example.util.simpletimetracker.feature_reminders.R
import com.example.util.simpletimetracker.feature_reminders.viewData.RemindersHeader
import com.example.util.simpletimetracker.feature_reminders.viewData.RemindersButtonViewData
import javax.inject.Inject

class RemindersViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val scheduledReminderInteractor: ScheduledReminderInteractor,
    private val reminderViewDataMapper: ReminderViewDataMapper,
    private val activityReminderOverrideRepo: ActivityReminderOverrideRepo,
    private val activityReminderViewDataMapper: ActivityReminderViewDataMapper,
    private val resourceRepo: ResourceRepo,
    private val recordTagViewDataMapper: RecordTagViewDataMapper,
) {

    suspend fun getViewData(): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val activityList = recordTypeInteractor.getAll()
        val activities = activityList.associateBy(RecordType::id)
        val categories = categoryInteractor.getAll().associateBy(Category::id)
        val tags = recordTagInteractor.getAll().associateBy(RecordTag::id)
        val activityReminderOverrides = activityReminderOverrideRepo.getAll()
            .associateBy { it.activityId }
        val reminders = getReminders()

        val activityReminders = mutableListOf<ViewHolderType>()
        activityReminders += HeaderViewData(
            section = RemindersHeader.Activity,
            text = resourceRepo.getString(R.string.notification_activity_title),
            hint = resourceRepo.getString(R.string.activity_reminders_hint),
        )
        activityReminders += activityList.mapNotNull { activity ->
            val override = activityReminderOverrides[activity.id] ?: return@mapNotNull null
            activityReminderViewDataMapper.map(
                activity = activity,
                override = override,
                isDarkTheme = isDarkTheme,
                useMilitaryTime = useMilitaryTime,
                firstDayOfWeek = firstDayOfWeek,
            )
        }
        activityReminders += reminderViewDataMapper.mapAddItem(
            id = RemindersButtonViewData.ACTIVITY,
            isDarkTheme = isDarkTheme,
        )

        val scheduledReminders = mutableListOf<ViewHolderType>()
        scheduledReminders += HeaderViewData(
            section = RemindersHeader.Scheduled,
            text = resourceRepo.getString(R.string.settings_reminders_title),
        )
        scheduledReminders += reminders.map { reminder ->
            val target = (reminder.schedule as? ScheduledReminder.Schedule.ActivityEvent)?.target
                ?: (reminder.condition as? ScheduledReminder.Condition.RecordsNotTrackedToday)?.target
            val targetName: String?
            val targetIcon: String?
            val targetColor: AppColor?
            when (target) {
                is ScheduledReminder.Condition.Target.Activity -> {
                    val data = activities[target.id]
                    targetName = data?.name
                    targetIcon = data?.icon
                    targetColor = data?.color
                }
                is ScheduledReminder.Condition.Target.Category -> {
                    val data = categories[target.id]
                    targetName = data?.name
                    targetIcon = " " // TODO hack to show empty icon
                    targetColor = data?.color
                }
                is ScheduledReminder.Condition.Target.Tag -> {
                    val data = tags[target.id]
                    targetName = data?.name
                    targetIcon = data
                        ?.let { recordTagViewDataMapper.mapIcon(it, activities) }
                    targetColor = data
                        ?.let { recordTagViewDataMapper.mapColor(it, activities) }
                }
                null -> {
                    targetName = null
                    targetIcon = null
                    targetColor = null
                }
            }
            reminderViewDataMapper.map(
                reminder = reminder,
                icon = targetIcon,
                color = targetColor,
                targetName = targetName,
                isDarkTheme = isDarkTheme,
                useMilitaryTime = useMilitaryTime,
                firstDayOfWeek = firstDayOfWeek,
            )
        }
        scheduledReminders += reminderViewDataMapper.mapAddItem(
            id = RemindersButtonViewData.SCHEDULED,
            isDarkTheme = isDarkTheme,
        )

        return listOf(
            scheduledReminders,
            activityReminders,
        ).flatten()
    }

    private suspend fun getReminders(): List<ScheduledReminder> {
        return scheduledReminderInteractor.getAll().sortedWith(
            compareBy<ScheduledReminder> {
                when (val schedule = it.schedule) {
                    is ScheduledReminder.Schedule.Hourly -> 0L
                    is ScheduledReminder.Schedule.Weekly -> 1L
                    is ScheduledReminder.Schedule.Monthly -> 2L
                    is ScheduledReminder.Schedule.OneTime -> 3L
                    is ScheduledReminder.Schedule.ActivityEvent -> when (schedule.event) {
                        RecordTimerEvent.STARTED -> 4L
                        RecordTimerEvent.STOPPED -> 5L
                    }
                }
            }.thenComparator { first, second ->
                when (val schedule = first.schedule) {
                    is ScheduledReminder.Schedule.Hourly -> compareValuesBy(
                        schedule,
                        second.schedule as? ScheduledReminder.Schedule.Hourly,
                        { it?.intervalSeconds },
                        { it?.startDate },
                        { it?.timeOfDayMillis },
                    )
                    is ScheduledReminder.Schedule.Weekly -> compareValuesBy(
                        schedule,
                        second.schedule as? ScheduledReminder.Schedule.Weekly,
                        { it?.timeOfDayMillis },
                    )
                    is ScheduledReminder.Schedule.Monthly -> compareValuesBy(
                        schedule,
                        second.schedule as? ScheduledReminder.Schedule.Monthly,
                        { it?.dayOfMonth },
                        { it?.timeOfDayMillis },
                    )
                    is ScheduledReminder.Schedule.OneTime -> compareValuesBy(
                        schedule,
                        second.schedule as? ScheduledReminder.Schedule.OneTime,
                        { it?.oneTimeDate },
                        { it?.timeOfDayMillis },
                    )
                    is ScheduledReminder.Schedule.ActivityEvent -> 0
                }
            }.thenBy(ScheduledReminder::id),
        )
    }
}

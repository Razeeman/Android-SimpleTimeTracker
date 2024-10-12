package com.example.util.simpletimetracker.feature_notification.activitySwitch.interactor

import com.example.util.simpletimetracker.core.interactor.FilterGoalsByDayOfWeekInteractor
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationActivitySwitchInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationActivitySwitchManager
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationActivitySwitchParams
import javax.inject.Inject

class NotificationActivitySwitchInteractorImpl @Inject constructor(
    private val manager: NotificationActivitySwitchManager,
    private val prefsInteractor: PrefsInteractor,
    private val resourceRepo: ResourceRepo,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val getCurrentRecordsDurationInteractor: GetCurrentRecordsDurationInteractor,
    private val timeMapper: TimeMapper,
    private val filterGoalsByDayOfWeekInteractor: FilterGoalsByDayOfWeekInteractor,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
    private val getNotificationActivitySwitchControlsInteractor: GetNotificationActivitySwitchControlsInteractor,
) : NotificationActivitySwitchInteractor {

    override suspend fun updateNotification(
        typesShift: Int,
        tagsShift: Int,
        selectedTypeId: Long,
    ) {
        if (prefsInteractor.getShowNotificationWithSwitch()) {
            if (shouldCancel()) {
                cancel()
            } else {
                show(
                    typesShift = typesShift,
                    tagsShift = tagsShift,
                    selectedTypeId = selectedTypeId,
                )
            }
        } else {
            cancel()
        }
    }

    private suspend fun show(
        typesShift: Int,
        tagsShift: Int,
        selectedTypeId: Long,
    ) {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val showRepeatButton = prefsInteractor.getEnableRepeatButton()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val runningRecords = runningRecordInteractor.getAll()
        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = RangeLength.Day,
            shift = 0,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        val viewedTags = if (selectedTypeId != 0L) {
            getSelectableTagsInteractor.execute(selectedTypeId)
                .filterNot { it.archived }
        } else {
            emptyList()
        }
        val recordTypes = recordTypeInteractor.getAll().associateBy(RecordType::id)
        val goals = filterGoalsByDayOfWeekInteractor.execute(
            goals = recordTypeGoalInteractor.getAllTypeGoals(),
            range = range,
            startOfDayShift = startOfDayShift,
        ).groupBy { it.idData.value }
        val allDailyCurrents = if (goals.isNotEmpty()) {
            getCurrentRecordsDurationInteractor.getAllDailyCurrents(
                typeIds = recordTypes.keys.toList(),
                runningRecords = runningRecords,
            )
        } else {
            // No goals - no need to calculate durations.
            emptyMap()
        }
        val controls = getNotificationActivitySwitchControlsInteractor.getControls(
            hintIsVisible = false,
            isDarkTheme = isDarkTheme,
            types = recordTypes.values.toList(),
            runningRecords = runningRecords,
            showRepeatButton = showRepeatButton,
            typesShift = typesShift,
            tags = viewedTags,
            tagsShift = tagsShift,
            selectedTypeId = selectedTypeId,
            goals = goals,
            allDailyCurrents = allDailyCurrents,
        )

        NotificationActivitySwitchParams(
            title = resourceRepo.getString(R.string.running_records_empty),
            isDarkTheme = prefsInteractor.getDarkMode(),
            controls = controls,
        ).let(manager::show)
    }

    private fun cancel() {
        manager.hide()
    }

    private suspend fun shouldCancel(): Boolean {
        return prefsInteractor.getShowNotificationWithSwitchHide() &&
            prefsInteractor.getShowNotifications() &&
            prefsInteractor.getShowNotificationsControls() &&
            !runningRecordInteractor.isEmpty()
    }
}
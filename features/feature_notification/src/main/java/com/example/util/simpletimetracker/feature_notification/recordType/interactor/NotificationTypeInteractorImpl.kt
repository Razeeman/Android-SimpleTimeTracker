package com.example.util.simpletimetracker.feature_notification.recordType.interactor

import com.example.util.simpletimetracker.core.interactor.CompleteTypesStateInteractor
import com.example.util.simpletimetracker.core.interactor.FilterGoalsByDayOfWeekInteractor
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTagViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.REPEAT_BUTTON_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.getDailyDuration
import com.example.util.simpletimetracker.domain.extension.getFullName
import com.example.util.simpletimetracker.domain.extension.getSessionDuration
import com.example.util.simpletimetracker.domain.extension.hasDailyDuration
import com.example.util.simpletimetracker.domain.extension.value
import com.example.util.simpletimetracker.domain.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.activitySwitch.interactor.GetNotificationActivitySwitchControlsInteractor
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsParams
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeManager
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeParams
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class NotificationTypeInteractorImpl @Inject constructor(
    private val notificationTypeManager: NotificationTypeManager,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val getCurrentRecordsDurationInteractor: GetCurrentRecordsDurationInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
    private val filterGoalsByDayOfWeekInteractor: FilterGoalsByDayOfWeekInteractor,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
    private val getNotificationActivitySwitchControlsInteractor: GetNotificationActivitySwitchControlsInteractor,
) : NotificationTypeInteractor {

    override suspend fun checkAndShow(
        typeId: Long,
        typesShift: Int,
        tagsShift: Int,
        selectedTypeId: Long,
    ) {
        if (!prefsInteractor.getShowNotifications()) return

        val recordType = recordTypeInteractor.get(typeId)
        val runningRecord = runningRecordInteractor.get(typeId)
        val recordTags = recordTagInteractor.getAll()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val showControls = prefsInteractor.getShowNotificationsControls()
        val showRepeatButton = prefsInteractor.getEnableRepeatButton()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = RangeLength.Day,
            shift = 0,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        val thisGoals = filterGoalsByDayOfWeekInteractor.execute(
            goals = recordTypeGoalInteractor.getByType(typeId),
            range = range,
            startOfDayShift = startOfDayShift,
        )
        val goalTime = if (thisGoals.hasDailyDuration()) {
            thisGoals.getDailyDuration()
        } else {
            thisGoals.getSessionDuration()
        }
        val viewedTags = if (selectedTypeId != 0L) {
            getSelectableTagsInteractor.execute(selectedTypeId)
                .filterNot { it.archived }
        } else {
            emptyList()
        }
        val controls = if (showControls) {
            val recordTypes = recordTypeInteractor.getAll().associateBy(RecordType::id)
            val goals = filterGoalsByDayOfWeekInteractor.execute(
                goals = recordTypeGoalInteractor.getAllTypeGoals(),
                range = range,
                startOfDayShift = startOfDayShift,
            ).groupBy { it.idData.value }
            val allDailyCurrents = if (goals.isNotEmpty()) {
                getCurrentRecordsDurationInteractor.getAllDailyCurrents(
                    typeIds = recordTypes.keys.toList(),
                    runningRecords = runningRecordInteractor.getAll(),
                )
            } else {
                // No goals - no need to calculate durations.
                emptyMap()
            }
            getNotificationActivitySwitchControlsInteractor.getControls(
                hintIsVisible = true,
                isDarkTheme = isDarkTheme,
                types = recordTypes.values.toList(),
                showRepeatButton = showRepeatButton,
                typesShift = typesShift,
                tags = viewedTags,
                tagsShift = tagsShift,
                selectedTypeId = selectedTypeId,
                goals = goals,
                allDailyCurrents = allDailyCurrents,
            )
        } else {
            NotificationControlsParams.Disabled
        }

        show(
            recordType = recordType,
            goalTime = goalTime,
            runningRecord = runningRecord ?: return,
            recordTags = recordTags.filter { it.id in runningRecord.tagIds },
            dailyCurrent = getCurrentRecordsDurationInteractor.getDailyCurrent(runningRecord),
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime,
            showSeconds = showSeconds,
            controls = controls,
        )
    }

    override suspend fun checkAndHide(typeId: Long) {
        if (!prefsInteractor.getShowNotifications()) return

        hide(typeId)
    }

    override suspend fun updateNotifications() {
        if (prefsInteractor.getShowNotifications()) {
            showAll()
        } else {
            hideAll()
        }
    }

    private suspend fun showAll() {
        val recordTypes = recordTypeInteractor.getAll().associateBy(RecordType::id)
        val recordTags = recordTagInteractor.getAll()
        val runningRecords = runningRecordInteractor.getAll()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val showControls = prefsInteractor.getShowNotificationsControls()
        val showRepeatButton = prefsInteractor.getEnableRepeatButton()
        val goals = filterGoalsByDayOfWeekInteractor
            .execute(recordTypeGoalInteractor.getAllTypeGoals())
            .groupBy { it.idData.value }
        val controls = if (showControls) {
            val allDailyCurrents = if (goals.isNotEmpty()) {
                getCurrentRecordsDurationInteractor.getAllDailyCurrents(
                    typeIds = recordTypes.keys.toList(),
                    runningRecords = runningRecords,
                )
            } else {
                // No goals - no need to calculate durations.
                emptyMap()
            }
            getNotificationActivitySwitchControlsInteractor.getControls(
                hintIsVisible = true,
                isDarkTheme = isDarkTheme,
                types = recordTypes.values.toList(),
                showRepeatButton = showRepeatButton,
                goals = goals,
                allDailyCurrents = allDailyCurrents,
            )
        } else {
            NotificationControlsParams.Disabled
        }

        runningRecords
            .forEach { runningRecord ->
                val thisGoals = goals[runningRecord.id].orEmpty()
                val goalTime = if (thisGoals.hasDailyDuration()) {
                    thisGoals.getDailyDuration()
                } else {
                    thisGoals.getSessionDuration()
                }
                show(
                    recordType = recordTypes[runningRecord.id],
                    goalTime = goalTime,
                    runningRecord = runningRecord,
                    recordTags = recordTags.filter { it.id in runningRecord.tagIds },
                    dailyCurrent = getCurrentRecordsDurationInteractor.getDailyCurrent(runningRecord),
                    isDarkTheme = isDarkTheme,
                    useMilitaryTime = useMilitaryTime,
                    showSeconds = showSeconds,
                    controls = controls,
                )
            }
    }

    private suspend fun hideAll() {
        recordTypeInteractor.getAll()
            .map(RecordType::id)
            .forEach { typeId -> hide(typeId) }
    }

    private fun show(
        recordType: RecordType?,
        goalTime: RecordTypeGoal?,
        runningRecord: RunningRecord,
        recordTags: List<RecordTag>,
        dailyCurrent: GetCurrentRecordsDurationInteractor.Result,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        showSeconds: Boolean,
        controls: NotificationControlsParams,
    ) {
        if (recordType == null) return

        NotificationTypeParams(
            id = recordType.id,
            icon = recordType.icon.let(iconMapper::mapIcon),
            color = colorMapper.mapToColorInt(recordType.color, isDarkTheme),
            text = getNotificationText(recordType, recordTags),
            timeStarted =
            timeMapper.formatTime(
                time = runningRecord.timeStarted,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ).let { resourceRepo.getString(R.string.notification_time_started, it) },
            startedTimeStamp = runningRecord.timeStarted,
            totalDuration = dailyCurrent.let {
                if (it.durationDiffersFromCurrent) it.duration else null
            },
            goalTime = goalTime.value
                .takeIf { it > 0 }
                ?.let(timeMapper::formatDuration)
                ?.let { resourceRepo.getString(R.string.running_record_goal_time, it) }
                .orEmpty(),
            stopButton = resourceRepo.getString(R.string.notification_record_type_stop),
            controls = controls,
        ).let(notificationTypeManager::show)
    }

    private fun getNotificationText(
        recordType: RecordType,
        recordTags: List<RecordTag>,
    ): String {
        val tag = recordTags.getFullName()

        return if (tag.isEmpty()) {
            recordType.name
        } else {
            "${recordType.name} - $tag"
        }
    }

    private fun hide(typeId: Long) {
        notificationTypeManager.hide(typeId.toInt())
    }
}
package com.example.util.simpletimetracker.feature_notification.recordType.interactor

import com.example.util.simpletimetracker.core.interactor.FilterGoalsByDayOfWeekInteractor
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.activitySuggestion.interactor.GetCurrentActivitySuggestionsInteractor
import com.example.util.simpletimetracker.domain.recordType.extension.getDailyDuration
import com.example.util.simpletimetracker.domain.recordType.extension.getSessionDuration
import com.example.util.simpletimetracker.domain.recordType.extension.hasDailyDuration
import com.example.util.simpletimetracker.domain.recordType.extension.value
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.activitySwitch.interactor.GetNotificationActivitySwitchControlsInteractor
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsParams
import com.example.util.simpletimetracker.feature_notification.core.NotificationCommonMapper
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeManager
import com.example.util.simpletimetracker.feature_notification.recordType.manager.NotificationTypeParams
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
    private val getNotificationActivitySwitchControlsInteractor: GetNotificationActivitySwitchControlsInteractor,
    private val notificationCommonMapper: NotificationCommonMapper,
    private val getCurrentActivitySuggestionsInteractor: GetCurrentActivitySuggestionsInteractor,
) : NotificationTypeInteractor {

    // TODO merge with update function?
    override suspend fun checkAndShow(
        typeId: Long,
        typesShift: Int,
        tagsShift: Int,
        selectedTypeId: Long,
        selectedTagId: Long,
        selectedTagValue: String?,
    ) {
        if (!prefsInteractor.getShowNotifications()) return

        val runningRecord = runningRecordInteractor.get(typeId) ?: return
        val recordType = recordTypeInteractor.get(typeId)
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
        val controls = if (showControls) {
            val runningRecords = runningRecordInteractor.getAll()
            val recordTypes = recordTypeInteractor.getAll().associateBy(RecordType::id)
            val suggestions = getCurrentActivitySuggestionsInteractor.execute(
                recordTypesMap = recordTypes,
                runningRecords = runningRecords,
            )
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
            getNotificationActivitySwitchControlsInteractor.getControls(
                hint = resourceRepo.getString(R.string.running_records_empty),
                isDarkTheme = isDarkTheme,
                types = recordTypes.values.toList(),
                suggestions = suggestions,
                showRepeatButton = showRepeatButton,
                typesShift = typesShift,
                tagsShift = tagsShift,
                selectedTypeId = selectedTypeId,
                selectedTagId = selectedTagId,
                selectedTagValue = selectedTagValue,
                goals = goals,
                allDailyCurrents = allDailyCurrents,
            )
        } else {
            NotificationControlsParams.Disabled
        }

        show(
            recordType = recordType,
            goal = goalTime,
            runningRecord = runningRecord,
            recordTags = recordTags,
            dailyCurrent = getCurrentRecordsDurationInteractor.getDailyCurrent(
                typeId = typeId,
                runningRecord = runningRecord,
            ),
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
        val suggestions = getCurrentActivitySuggestionsInteractor.execute(
            recordTypesMap = recordTypes,
            runningRecords = runningRecords,
        )
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
                hint = resourceRepo.getString(R.string.running_records_empty),
                isDarkTheme = isDarkTheme,
                types = recordTypes.values.toList(),
                suggestions = suggestions,
                showRepeatButton = showRepeatButton,
                typesShift = 0,
                tagsShift = 0,
                selectedTypeId = null,
                selectedTagId = null,
                selectedTagValue = null,
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
                    goal = goalTime,
                    runningRecord = runningRecord,
                    recordTags = recordTags,
                    dailyCurrent = getCurrentRecordsDurationInteractor.getDailyCurrent(
                        typeId = runningRecord.id,
                        runningRecord = runningRecord,
                    ),
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
        goal: RecordTypeGoal?,
        runningRecord: RunningRecord,
        recordTags: List<RecordTag>,
        dailyCurrent: GetCurrentRecordsDurationInteractor.Result,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        showSeconds: Boolean,
        controls: NotificationControlsParams,
    ) {
        if (recordType == null) return

        val tagIds = runningRecord.tags.map(RecordBase.Tag::tagId)
        val goalSubtype = goal?.subtype ?: RecordTypeGoal.Subtype.Goal
        val goalSubtypeString = when (goalSubtype) {
            is RecordTypeGoal.Subtype.Goal -> R.string.change_record_type_goal_time_hint
            is RecordTypeGoal.Subtype.Limit -> R.string.change_record_type_limit_time_hint
        }.let(resourceRepo::getString).lowercase()
        val goalTime = goal.value
            .takeIf { it > 0 }
            ?.let(timeMapper::formatDuration)
            ?.let { "$goalSubtypeString $it" }
            .orEmpty()

        NotificationTypeParams(
            id = recordType.id,
            icon = recordType.icon.let(iconMapper::mapIcon),
            color = colorMapper.mapToColorInt(recordType.color, isDarkTheme),
            text = notificationCommonMapper.getNotificationText(
                recordType = recordType,
                recordTags = recordTags.filter { it.id in tagIds },
                recordTagsData = runningRecord.tags,
            ),
            timeStarted = timeMapper.formatTime(
                time = runningRecord.timeStarted,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ).let { resourceRepo.getString(R.string.notification_time_started, it) },
            startedTimeStamp = runningRecord.timeStarted,
            totalDuration = dailyCurrent.let {
                if (it.durationDiffersFromCurrent) it.duration else null
            },
            goalTime = goalTime,
            stopButton = resourceRepo.getString(R.string.notification_record_type_stop),
            controls = controls,
        ).let(notificationTypeManager::show)
    }

    private fun hide(typeId: Long) {
        notificationTypeManager.hide(typeId.toInt())
    }
}
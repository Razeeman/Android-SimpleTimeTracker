package com.example.util.simpletimetracker.domain.record.interactor

import com.example.util.simpletimetracker.domain.notifications.interactor.ActivityStartedStoppedBroadcastInteractor
import com.example.util.simpletimetracker.domain.complexRule.interactor.ComplexRuleProcessActionInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationGoalCountInteractor
import com.example.util.simpletimetracker.domain.pomodoro.interactor.PomodoroStartInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTypeToDefaultTagInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.base.ResultContainer
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RecordDataSelectionDialogResult
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddRunningRecordMediator @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val recordTypeToDefaultTagInteractor: RecordTypeToDefaultTagInteractor,
    private val notificationGoalCountInteractor: NotificationGoalCountInteractor,
    private val activityStartedStoppedBroadcastInteractor: ActivityStartedStoppedBroadcastInteractor,
    private val shouldShowRecordDataSelectionInteractor: ShouldShowRecordDataSelectionInteractor,
    private val pomodoroStartInteractor: PomodoroStartInteractor,
    private val complexRuleProcessActionInteractor: ComplexRuleProcessActionInteractor,
    private val updateExternalViewsInteractor: UpdateExternalViewsInteractor,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {

    /**
     * Returns true if activity was started.
     */
    suspend fun tryStartTimer(
        typeId: Long,
        updateNotificationSwitch: Boolean = true,
        commentInputAvailable: Boolean = true,
        onNeedToShowTagSelection: suspend (RecordDataSelectionDialogResult) -> Unit,
    ): Boolean {
        // Already running
        if (runningRecordInteractor.get(typeId) != null) return false

        val shouldShowTagSelectionResult = shouldShowRecordDataSelectionInteractor.execute(
            typeId = typeId,
            commentInputAvailable = commentInputAvailable,
        )
        return if (shouldShowTagSelectionResult.fields.isNotEmpty()) {
            onNeedToShowTagSelection(shouldShowTagSelectionResult)
            false
        } else {
            startTimer(
                typeId = typeId,
                tags = emptyList(),
                comment = "",
                updateNotificationSwitch = updateNotificationSwitch,
            )
            true
        }
    }

    suspend fun startTimer(
        typeId: Long,
        tags: List<RecordBase.Tag>,
        comment: String,
        timeStarted: StartTime = StartTime.TakeCurrent,
        updateNotificationSwitch: Boolean = true,
        checkDefaultDuration: Boolean = true,
    ) {
        val currentTime = currentTimestampProvider.get()
        val actualTimeStarted = when (timeStarted) {
            is StartTime.Current -> timeStarted.currentTimeStampMs
            is StartTime.TakeCurrent -> currentTime
            is StartTime.Timestamp -> timeStarted.timestampMs
        }.coerceAtMost(currentTime)
        val retroactiveTrackingMode = prefsInteractor.getRetroactiveTrackingMode()
        val actualPrevRecords = if (
            retroactiveTrackingMode ||
            complexRuleProcessActionInteractor.hasRules()
        ) {
            recordInteractor.getAllPrev(actualTimeStarted)
        } else {
            emptyList()
        }
        val rulesResult = if (
            retroactiveTrackingMode &&
            getPrevRecordToMergeWith(typeId, actualPrevRecords) != null
        ) {
            // No need to check rules on merge.
            ComplexRuleProcessActionInteractor.Result(
                isMultitaskingAllowed = ResultContainer.Undefined,
                tagsIds = emptySet(),
            )
        } else {
            processRules(
                typeId = typeId,
                timeStarted = actualTimeStarted,
                prevRecords = actualPrevRecords,
            )
        }
        val isMultitaskingAllowedByDefault = prefsInteractor.getAllowMultitasking()
        val isMultitaskingAllowed = rulesResult.isMultitaskingAllowed.getValueOrNull()
            ?: isMultitaskingAllowedByDefault
        processMultitasking(
            typeId = typeId,
            isMultitaskingAllowed = isMultitaskingAllowed,
            splitTime = when (timeStarted) {
                is StartTime.Current -> timeStarted.currentTimeStampMs
                is StartTime.TakeCurrent -> currentTime
                is StartTime.Timestamp -> currentTime
            },
        )
        val actualTags = getAllTags(
            typeId = typeId,
            currentTags = tags,
            tagIdsFromRules = rulesResult.tagsIds,
        )
        activityStartedStoppedBroadcastInteractor.onActionActivityStarted(
            typeId = typeId,
            tagIds = actualTags.map { it.tagId },
            comment = comment,
        )
        val startParams = StartParams(
            typeId = typeId,
            comment = comment,
            tags = actualTags,
            timeStarted = actualTimeStarted,
            updateNotificationSwitch = updateNotificationSwitch,
            isMultitaskingAllowed = isMultitaskingAllowed,
        )
        if (retroactiveTrackingMode) {
            addRetroactiveModeInternal(startParams, actualPrevRecords)
        } else {
            addInternal(startParams, checkDefaultDuration)
        }
        // Show goal count only on timer start, otherwise it would show on change also.
        notificationGoalCountInteractor.checkAndShow(typeId)
        if (!retroactiveTrackingMode) {
            pomodoroStartInteractor.checkAndStart(typeId)
        }
    }

    // Used separately only for changing running activity,
    // due to some poor (probably) decisions id of running record is it's type id,
    // so if type is changed - need to remove old and add new data.
    suspend fun addAfterChange(
        typeId: Long,
        timeStarted: Long,
        comment: String,
        tags: List<RecordBase.Tag>,
    ) {
        addInternal(
            params = StartParams(
                typeId = typeId,
                timeStarted = timeStarted,
                comment = comment,
                tags = tags,
                updateNotificationSwitch = true,
                isMultitaskingAllowed = true,
            ),
            checkDefaultDuration = false,
        )
    }

    private suspend fun addInternal(
        params: StartParams,
        checkDefaultDuration: Boolean,
    ) {
        val type = recordTypeInteractor.get(params.typeId) ?: return
        if (type.defaultDuration > 0L && checkDefaultDuration) {
            addInstantRecord(params, type)
        } else {
            addRunningRecord(params)
        }
    }

    private suspend fun addRetroactiveModeInternal(
        params: StartParams,
        prevRecords: List<Record>,
    ) {
        val type = recordTypeInteractor.get(params.typeId) ?: return

        processRetroactiveMultitasking(
            params = params,
            prevRecords = prevRecords,
        )

        if (type.defaultDuration > 0L) {
            val newTimeStarted = params.timeStarted - type.defaultDuration * 1000
            addInstantRecord(
                params = params.copy(timeStarted = newTimeStarted),
                type = type,
            )
        } else {
            addRecordRetroactively(
                params = params,
                prevRecords = prevRecords,
            )
        }
    }

    private suspend fun addRunningRecord(
        params: StartParams,
    ) {
        if (runningRecordInteractor.get(params.typeId) == null && params.typeId > 0L) {
            val data = RunningRecord(
                id = params.typeId,
                timeStarted = params.timeStarted,
                comment = params.comment,
                tags = params.tags,
            )

            runningRecordInteractor.add(data)
            updateExternalViewsInteractor.onRunningRecordAdd(
                typeId = params.typeId,
                tagIds = params.tags.map(RecordBase.Tag::tagId),
                updateNotificationSwitch = params.updateNotificationSwitch,
            )
        }
    }

    private suspend fun addInstantRecord(
        params: StartParams,
        type: RecordType,
    ) {
        Record(
            typeId = params.typeId,
            timeStarted = params.timeStarted,
            timeEnded = params.timeStarted + type.defaultDuration * 1000,
            comment = params.comment,
            tags = params.tags,
        ).let {
            addRecordMediator.add(
                record = it,
                updateNotificationSwitch = params.updateNotificationSwitch,
            )
        }
    }

    private suspend fun addRecordRetroactively(
        params: StartParams,
        prevRecords: List<Record>,
    ) {
        val prevRecord = getPrevRecordToMergeWith(params.typeId, prevRecords)
        val sameTags = prevRecord?.tags.orEmpty().sortedBy { it.tagId } == params.tags.sortedBy { it.tagId }
        val shouldMerge = sameTags || params.tags.isEmpty()

        val record = if (prevRecord != null && shouldMerge) {
            Record(
                id = prevRecord.id, // Updates existing record.
                typeId = params.typeId,
                timeStarted = prevRecord.timeStarted,
                timeEnded = params.timeStarted,
                comment = params.comment.takeUnless { it.isEmpty() }
                    ?: prevRecord.comment,
                tags = params.tags.takeUnless { it.isEmpty() }
                    ?: prevRecord.tags,
            )
        } else {
            val newTimeStarted = prevRecords.firstOrNull()?.timeEnded
                ?: (params.timeStarted - TimeUnit.MINUTES.toMillis(5))
            Record(
                id = 0L, // Creates new record.
                typeId = params.typeId,
                timeStarted = newTimeStarted,
                timeEnded = params.timeStarted,
                comment = params.comment,
                tags = params.tags,
            )
        }
        addRecordMediator.add(
            record = record,
            updateNotificationSwitch = params.updateNotificationSwitch,
        )
    }

    private suspend fun processRules(
        typeId: Long,
        timeStarted: Long,
        prevRecords: List<Record>,
    ): ComplexRuleProcessActionInteractor.Result {
        // If no rules - no need to check them.
        return if (complexRuleProcessActionInteractor.hasRules()) {
            val currentRecords = runningRecordInteractor.getAll()
            val hasAnyRunningTimersOnTimeStarted = currentRecords.any {
                it.timeStarted <= timeStarted
            }
            val takeCurrentRecords = currentRecords.isNotEmpty() &&
                hasAnyRunningTimersOnTimeStarted

            // If no current records - check closest previous.
            val records = if (takeCurrentRecords) currentRecords else prevRecords

            val currentTypeIds = records
                .map { it.typeIds }
                .flatten()
                .toSet()

            complexRuleProcessActionInteractor.processRules(
                timeStarted = timeStarted,
                startingTypeId = typeId,
                currentTypeIds = currentTypeIds,
            )
        } else {
            ComplexRuleProcessActionInteractor.Result(
                isMultitaskingAllowed = ResultContainer.Undefined,
                tagsIds = emptySet(),
            )
        }
    }

    private suspend fun processMultitasking(
        typeId: Long,
        isMultitaskingAllowed: Boolean,
        splitTime: Long,
    ) {
        // Stop running records if multitasking is disabled.
        if (!isMultitaskingAllowed) {
            // Widgets will update on adding.
            runningRecordInteractor.getAll()
                .filter { it.id != typeId }
                .forEach {
                    removeRunningRecordMediator.removeWithRecordAdd(
                        runningRecord = it,
                        updateWidgets = false,
                        updateNotificationSwitch = false,
                        timeEnded = splitTime,
                    )
                }
        }
    }

    private suspend fun processRetroactiveMultitasking(
        params: StartParams,
        prevRecords: List<Record>,
    ) {
        if (!params.isMultitaskingAllowed) return

        val recordTypesMap = recordTypeInteractor.getAll().associateBy(RecordType::id)
        val mergedRecord = getPrevRecordToMergeWith(params.typeId, prevRecords)

        // Extend prev records to current time.
        prevRecords.filter {
            // Skip record that would be merge.
            it.id != mergedRecord?.id
        }.filter {
            // Skip records with default duration.
            val thisType = recordTypesMap[it.typeId]
            thisType != null && thisType.defaultDuration == 0L
        }.map { prevRecord ->
            recordInteractor.updateTimeEnded(
                recordId = prevRecord.id,
                timeEnded = params.timeStarted,
            )
            prevRecord
        }.let {
            updateExternalViewsInteractor.onRecordTimeEndedChange(
                typeIds = it.map(Record::typeId),
                tagIds = it.map(Record::tags).flatten().map(RecordBase.Tag::tagId).distinct(),
            )
        }
    }

    private suspend fun getAllTags(
        typeId: Long,
        currentTags: List<RecordBase.Tag>,
        tagIdsFromRules: Set<Long>,
    ): List<RecordBase.Tag> {
        val defaultTags = recordTypeToDefaultTagInteractor.getTags(typeId)
        // TODO TAG add tag value to default tags and rules?
        val result = currentTags +
            defaultTags.map { RecordBase.Tag(tagId = it, numericValue = null) } +
            tagIdsFromRules.map { RecordBase.Tag(tagId = it, numericValue = null) }
        return result.distinctBy { it.tagId }
    }

    private fun getPrevRecordToMergeWith(
        typeId: Long,
        prevRecords: List<Record>,
    ): Record? {
        return prevRecords.firstOrNull { it.typeId == typeId }
    }

    private data class StartParams(
        val typeId: Long,
        val timeStarted: Long,
        val comment: String,
        val tags: List<RecordBase.Tag>,
        val updateNotificationSwitch: Boolean,
        val isMultitaskingAllowed: Boolean,
    )

    sealed interface StartTime {
        data class Current(val currentTimeStampMs: Long) : StartTime
        data class Timestamp(val timestampMs: Long) : StartTime
        object TakeCurrent : StartTime
    }
}
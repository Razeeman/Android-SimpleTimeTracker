package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.ResultContainer
import com.example.util.simpletimetracker.domain.model.RunningRecord
import javax.inject.Inject

class AddRunningRecordMediator @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val recordTypeToDefaultTagInteractor: RecordTypeToDefaultTagInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationInactivityInteractor: NotificationInactivityInteractor,
    private val notificationActivityInteractor: NotificationActivityInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val notificationGoalCountInteractor: NotificationGoalCountInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val wearInteractor: WearInteractor,
    private val activityStartedStoppedBroadcastInteractor: ActivityStartedStoppedBroadcastInteractor,
    private val shouldShowTagSelectionInteractor: ShouldShowTagSelectionInteractor,
    private val pomodoroStartInteractor: PomodoroStartInteractor,
    private val complexRuleProcessActionInteractor: ComplexRuleProcessActionInteractor,
) {

    /**
     * Returns true if activity was started.
     */
    suspend fun tryStartTimer(
        typeId: Long,
        onNeedToShowTagSelection: suspend () -> Unit,
    ): Boolean {
        // Already running
        if (runningRecordInteractor.get(typeId) != null) return false

        return if (shouldShowTagSelectionInteractor.execute(typeId)) {
            onNeedToShowTagSelection()
            false
        } else {
            startTimer(typeId, emptyList(), "")
            true
        }
    }

    suspend fun startTimer(
        typeId: Long,
        tagIds: List<Long>,
        comment: String,
        timeStarted: Long? = null,
    ) {
        val rulesResult = processRules(
            typeId = typeId,
            timeStarted = timeStarted ?: System.currentTimeMillis(),
        )
        processMultitasking(
            typeId = typeId,
            isMultitaskingAllowedByRules = rulesResult.isMultitaskingAllowed,
        )
        val actualTags = getAllTags(
            typeId = typeId,
            tagIds = tagIds,
            tagIdsFromRules = rulesResult.tagsIds,
        )
        activityStartedStoppedBroadcastInteractor.onActionActivityStarted(
            typeId = typeId,
            tagIds = actualTags,
            comment = comment,
        )
        addInternal(
            StartParams(
                typeId = typeId,
                comment = comment,
                tagIds = actualTags,
                timeStarted = timeStarted ?: System.currentTimeMillis(),
            ),
        )
        // Show goal count only on timer start, otherwise it would show on change also.
        notificationGoalCountInteractor.checkAndShow(typeId)
        pomodoroStartInteractor.checkAndStart(typeId)
    }

    // Used separately only for changing running activity,
    // due to some poor (probably) decisions id of running record is it's type id,
    // so if type is changed - need to remove old and add new data.
    suspend fun addAfterChange(
        typeId: Long,
        timeStarted: Long,
        comment: String,
        tagIds: List<Long>,
    ) {
        addInternal(
            StartParams(
                typeId = typeId,
                timeStarted = timeStarted,
                comment = comment,
                tagIds = tagIds,
            ),
        )
    }

    private suspend fun addInternal(params: StartParams) {
        val type = recordTypeInteractor.get(params.typeId) ?: return
        if (type.instant) {
            addInstantRecord(params, type)
        } else {
            addRunningRecord(params)
        }
    }

    private suspend fun addRunningRecord(
        params: StartParams,
    ) {
        if (runningRecordInteractor.get(params.typeId) == null && params.typeId > 0L) {
            RunningRecord(
                id = params.typeId,
                timeStarted = params.timeStarted,
                comment = params.comment,
                tagIds = params.tagIds,
            ).let {
                runningRecordInteractor.add(it)
                notificationTypeInteractor.checkAndShow(params.typeId)
                notificationInactivityInteractor.cancel()
                // Schedule only on first activity start.
                if (runningRecordInteractor.getAll().size == 1) notificationActivityInteractor.checkAndSchedule()
                notificationGoalTimeInteractor.checkAndReschedule(listOf(params.typeId))
                widgetInteractor.updateWidgets()
                wearInteractor.update()
            }
        }
    }

    private suspend fun addInstantRecord(
        params: StartParams,
        type: RecordType,
    ) {
        Record(
            typeId = params.typeId,
            timeStarted = params.timeStarted,
            timeEnded = params.timeStarted + type.instantDuration * 1000,
            comment = params.comment,
            tagIds = params.tagIds
        ).let {
            addRecordMediator.add(it)
        }
    }

    private suspend fun processRules(
        typeId: Long,
        timeStarted: Long,
    ): ComplexRuleProcessActionInteractor.Result {
        // If no rules - no need to check them.
        return if (complexRuleProcessActionInteractor.hasRules()) {
            // Check running records but also record that are recorded for this time.
            val currentRecords = runningRecordInteractor.getAll() +
                recordInteractor.getFromRange(Range(timeStarted, timeStarted))

            // If no current records - check closest previous.
            val prevRecord = if (currentRecords.isEmpty()) {
                recordInteractor.getPrev(timeStarted = timeStarted, limit = 1)
            } else {
                emptySet()
            }

            val currentTypeIds = (currentRecords + prevRecord)
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
        isMultitaskingAllowedByRules: ResultContainer<Boolean>,
    ) {
        val isMultitaskingAllowedByDefault = prefsInteractor.getAllowMultitasking()
        val isMultitaskingAllowed = isMultitaskingAllowedByRules.getValueOrNull()
            ?: isMultitaskingAllowedByDefault

        // Stop running records if multitasking is disabled.
        if (!isMultitaskingAllowed) {
            // Widgets will update on adding.
            runningRecordInteractor.getAll()
                .filter { it.id != typeId }
                .forEach { removeRunningRecordMediator.removeWithRecordAdd(it, updateWidgets = false) }
        }
    }

    private suspend fun getAllTags(
        typeId: Long,
        tagIds: List<Long>,
        tagIdsFromRules: Set<Long>,
    ): List<Long> {
        val defaultTags = recordTypeToDefaultTagInteractor.getTags(typeId)
        return (tagIds + defaultTags + tagIdsFromRules).toSet().toList()
    }

    private data class StartParams(
        val typeId: Long,
        val timeStarted: Long,
        val comment: String,
        val tagIds: List<Long>,
    )
}
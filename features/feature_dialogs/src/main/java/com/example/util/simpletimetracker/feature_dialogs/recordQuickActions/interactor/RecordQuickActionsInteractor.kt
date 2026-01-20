package com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.interactor

import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordTag.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.navigation.params.screen.RecordQuickActionsParams.Type
import java.util.Calendar
import javax.inject.Inject

class RecordQuickActionsInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
) {

    @Suppress("ComplexRedundantLet")
    suspend fun changeType(
        params: List<Type>,
        newTypeId: Long,
    ) {
        params.filterIsInstance<Type.RecordTracked>().let { trackedParams ->
            val recordIds = trackedParams.map(Type.RecordTracked::id)
            val records = recordIds
                .mapNotNull { recordInteractor.get(it) }
                .filter { it.typeId != newTypeId }
            changeRecordType(
                old = records,
                newTypeId = newTypeId,
            )
        }

        params.filterIsInstance<Type.RecordUntracked>().let { untrackedParams ->
            changeUntrackedType(
                params = untrackedParams,
                newTypeId = newTypeId,
            )
        }

        params.filterIsInstance<Type.RecordRunning>().let { runningParams ->
            val recordIds = runningParams.map(Type.RecordRunning::id)
            val records = recordIds
                .mapNotNull { runningRecordInteractor.get(it) }
                .filter { it.id != newTypeId }
            changeRunningRecordType(
                old = records,
                newTypeId = newTypeId,
            )
        }
    }

    suspend fun changeTags(
        params: List<Type>,
        newTags: List<RecordBase.Tag>,
    ) {
        params.filterIsInstance<Type.RecordTracked>().let { trackedParams ->
            val recordIds = trackedParams.map(Type.RecordTracked::id)
            val records = recordIds
                .mapNotNull { recordInteractor.get(it) }
                .filter {
                    it.tags.sortedBy(RecordBase.Tag::tagId) !=
                        newTags.sortedBy(RecordBase.Tag::tagId)
                }
            changeRecordTags(
                old = records,
                newTags = newTags,
            )
        }
        params.filterIsInstance<Type.RecordUntracked>().let {
            // Do nothing. Should not be possible.
        }
        params.filterIsInstance<Type.RecordRunning>().let { runningParams ->
            val recordIds = runningParams.map { it.id }
            val records = recordIds
                .mapNotNull { runningRecordInteractor.get(it) }
                .filter {
                    it.tags.sortedBy(RecordBase.Tag::tagId) !=
                        newTags.sortedBy(RecordBase.Tag::tagId)
                }
            changeRunningRecordTags(
                old = records,
                newTags = newTags,
            )
        }
    }

    suspend fun move(
        params: List<Type>,
        timestamp: Long,
        changeOnlyDate: Boolean,
    ) {
        val recordIds = params.filterIsInstance<Type.RecordTracked>().map { it.id }
        val records = recordIds.mapNotNull { recordInteractor.get(it) }
        val calendar = Calendar.getInstance()

        records.mapNotNull { record ->
            val currentDuration = record.duration
            val newTimeStarted = if (changeOnlyDate) {
                changeOnlyDate(
                    currentTimestamp = record.timeStarted,
                    timestampWithDateSelected = timestamp,
                    calendar = calendar,
                )
            } else {
                timestamp
            }
            if (newTimeStarted == record.timeStarted) return@mapNotNull null
            record.copy(
                timeStarted = newTimeStarted,
                timeEnded = newTimeStarted + currentDuration,
            )
        }.let {
            addRecordMediator.add(it)
        }
    }

    private suspend fun changeRecordType(
        old: List<Record>,
        newTypeId: Long,
    ) {
        old.map { oldRecord ->
            oldRecord.copy(
                typeId = newTypeId,
                tags = getTagsAfterActivityChange(
                    currentTags = oldRecord.tags,
                    newTypeId = newTypeId,
                ),
            )
        }.let {
            addRecordMediator.add(it)
        }
        old.mapNotNull {
            if (it.typeId != newTypeId) it.typeId else null
        }.let { externalViewsInteractor.onRecordChangeType(it) }
    }

    private suspend fun changeRecordTags(
        old: List<Record>,
        newTags: List<RecordBase.Tag>,
    ) {
        old.map { oldRecord ->
            oldRecord.copy(tags = newTags)
        }.let {
            addRecordMediator.add(it)
        }
        val newTagIds = newTags.map(RecordBase.Tag::tagId)
        old.map { oldRecord ->
            oldRecord.tags.map(RecordBase.Tag::tagId).filter { it !in newTagIds }
        }.flatten().takeIf {
            it.isNotEmpty()
        }?.let { externalViewsInteractor.onRecordChangeTags(it) }
    }

    private suspend fun changeRunningRecordType(
        old: List<RunningRecord>,
        newTypeId: Long,
    ) {
        old.forEach { oldRecord ->
            // Widgets will update on adding.
            removeRunningRecordMediator.remove(
                typeId = oldRecord.id,
                updateWidgets = false,
                updateNotificationSwitch = false,
                checkPomodoroStop = oldRecord.id != newTypeId,
            )
        }
        old.firstOrNull()?.let { oldRecord ->
            addRunningRecordMediator.addAfterChange(
                typeId = newTypeId,
                timeStarted = oldRecord.timeStarted,
                comment = oldRecord.comment,
                tags = getTagsAfterActivityChange(
                    currentTags = oldRecord.tags,
                    newTypeId = newTypeId,
                ),
            )
        }
    }

    private suspend fun changeRunningRecordTags(
        old: List<RunningRecord>,
        newTags: List<RecordBase.Tag>,
    ) {
        old.forEach { oldRecord ->
            oldRecord.copy(
                tags = newTags,
            ).let {
                runningRecordInteractor.add(it)
            }
        }
        old.forEach { oldRecord ->
            externalViewsInteractor.onRunningRecordAdd(
                typeId = oldRecord.id,
                tagIds = (oldRecord.tags + newTags).map(RecordBase.Tag::tagId).distinct(),
                updateNotificationSwitch = true,
            )
        }
    }

    private suspend fun changeUntrackedType(
        params: List<Type.RecordUntracked>,
        newTypeId: Long,
    ) {
        params.map { untrackedRecord ->
            val newTimeStarted = untrackedRecord.timeStarted
            val newTimeEnded = untrackedRecord.timeEnded
            Record(
                id = 0L,
                typeId = newTypeId,
                timeStarted = newTimeStarted,
                timeEnded = newTimeEnded,
                comment = "",
                tags = emptyList(),
            )
        }.let {
            addRecordMediator.add(it)
        }
    }

    private suspend fun getTagsAfterActivityChange(
        currentTags: List<RecordBase.Tag>,
        newTypeId: Long,
    ): List<RecordBase.Tag> {
        val selectableTags = getSelectableTagsInteractor.execute(newTypeId)
            .map(RecordTag::id)
        return currentTags.filter { it.tagId in selectableTags }
    }

    private fun changeOnlyDate(
        currentTimestamp: Long,
        timestampWithDateSelected: Long,
        calendar: Calendar,
    ): Long {
        calendar.timeInMillis = timestampWithDateSelected
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        calendar.apply {
            timeInMillis = currentTimestamp
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }

        return calendar.timeInMillis
    }
}
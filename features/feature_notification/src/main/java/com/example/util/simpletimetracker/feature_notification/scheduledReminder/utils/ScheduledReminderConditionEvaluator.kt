package com.example.util.simpletimetracker.feature_notification.scheduledReminder.utils

import com.example.util.simpletimetracker.core.interactor.GetRangeInteractor
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.category.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import javax.inject.Inject

class ScheduledReminderConditionEvaluator @Inject constructor(
    private val getRangeInteractor: GetRangeInteractor,
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
) {

    suspend fun shouldShow(condition: ScheduledReminder.Condition): Boolean {
        return when (condition) {
            is ScheduledReminder.Condition.Always -> true
            is ScheduledReminder.Condition.RecordsNotTrackedToday -> {
                isTargetNotTrackedToday(condition.target)
            }
        }
    }

    private suspend fun isTargetNotTrackedToday(
        target: ScheduledReminder.Condition.Target,
    ): Boolean {
        val range = getRangeInteractor.getRange(RangeLength.Day)
        val hasCompletedRecord = recordInteractor.getWithParams(
            param = RecordInteractor.GetParam.FromRange(range),
        ).any { record ->
            matches(target, record)
        }
        if (hasCompletedRecord) return false

        return runningRecordInteractor.getAll().none { runningRecord ->
            val runningRange = Range(
                timeStarted = runningRecord.timeStarted,
                timeEnded = currentTimestampProvider.get(),
            )
            runningRange.isOverlappingWith(range) && matches(target, runningRecord)
        }
    }

    private suspend fun matches(
        target: ScheduledReminder.Condition.Target,
        record: RecordBase,
    ): Boolean {
        val activityId = record.typeIds.firstOrNull() ?: return false
        val tagIds = record.tags.map(RecordBase.Tag::tagId)
        return when (target) {
            is ScheduledReminder.Condition.Target.Activity ->
                activityId == target.id
            is ScheduledReminder.Condition.Target.Category ->
                activityId in recordTypeCategoryInteractor.getTypes(target.id)
            is ScheduledReminder.Condition.Target.Tag ->
                target.id in tagIds
        }
    }
}

package com.example.util.simpletimetracker.feature_notification.scheduledReminder.utils

import com.example.util.simpletimetracker.core.interactor.GetRangeInteractor
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import javax.inject.Inject

class ScheduledReminderConditionEvaluator @Inject constructor(
    private val getRangeInteractor: GetRangeInteractor,
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {

    suspend fun shouldShow(condition: ScheduledReminder.Condition): Boolean {
        return when (condition) {
            is ScheduledReminder.Condition.Always -> true
            is ScheduledReminder.Condition.ActivityNotTrackedToday -> {
                isActivityNotTrackedToday(condition.activityId)
            }
        }
    }

    private suspend fun isActivityNotTrackedToday(activityId: Long): Boolean {
        val range = getRangeInteractor.getRange(RangeLength.Day)
        val hasCompletedRecord = recordInteractor.getWithParams(
            param = RecordInteractor.GetParam.FromRangeByType(ids = setOf(activityId), range = range),
        ).isNotEmpty()
        if (hasCompletedRecord) return false

        val runningRecord = runningRecordInteractor.get(activityId) ?: return true
        val runningRange = Range(
            timeStarted = runningRecord.timeStarted,
            timeEnded = currentTimestampProvider.get(),
        )
        return !runningRange.isOverlappingWith(range)
    }
}

package com.example.util.simpletimetracker.feature_notification.goalTime.interactor

import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.domain.extension.value
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalRangeEndInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal.Range
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_notification.goalTime.manager.NotificationGoalTimeManager
import com.example.util.simpletimetracker.feature_notification.goalTime.scheduler.NotificationGoalTimeScheduler
import javax.inject.Inject

class NotificationGoalTimeInteractorImpl @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val getCurrentRecordsDurationInteractor: GetCurrentRecordsDurationInteractor,
    private val manager: NotificationGoalTimeManager,
    private val scheduler: NotificationGoalTimeScheduler,
    private val notificationGoalRangeEndInteractor: NotificationGoalRangeEndInteractor,
    private val notificationGoalParamsInteractor: NotificationGoalParamsInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
) : NotificationGoalTimeInteractor {

    override suspend fun checkAndReschedule() {
        runningRecordInteractor.getAll()
            .map(RunningRecord::id)
            .let { checkAndReschedule(it) }
    }

    override suspend fun checkAndReschedule(typeIds: List<Long>) {
        typeIds.forEach { checkAndReschedule(it) }
        notificationGoalRangeEndInteractor.checkAndReschedule()
    }

    override suspend fun checkAndRescheduleCategory(categoryId: Long) {
        val typeIds = recordTypeCategoryInteractor.getTypes(categoryId)
        checkAndReschedule(typeIds)
    }

    override fun cancel(typeId: Long) {
        listOf(
            Range.Session,
            Range.Daily,
            Range.Weekly,
            Range.Monthly,
        ).forEach {
            scheduler.cancelSchedule(RecordTypeGoal.IdData.Type(typeId), it)
            manager.hide(typeId, it)
        }
    }

    override suspend fun show(
        idData: RecordTypeGoal.IdData,
        goalRange: Range,
    ) {
        notificationGoalParamsInteractor.execute(
            idData = idData,
            range = goalRange,
            type = NotificationGoalParamsInteractor.Type.Duration,
        ).let(manager::show)
    }

    private suspend fun checkAndReschedule(typeId: Long) {
        val recordType = recordTypeInteractor.get(typeId)
        val runningRecord = runningRecordInteractor.get(typeId)
        val goals = recordTypeGoalInteractor.getByType(typeId)

        if (recordType == null || runningRecord == null) return

        cancel(typeId)

        // Session
        check(
            goalRange = Range.Session,
            idData = RecordTypeGoal.IdData.Type(typeId),
            goals = goals,
            runningRecord = runningRecord,
        )

        // Daily
        check(
            goalRange = Range.Daily,
            idData = RecordTypeGoal.IdData.Type(typeId),
            goals = goals,
            runningRecord = runningRecord,
        )

        // Weekly
        check(
            goalRange = Range.Weekly,
            idData = RecordTypeGoal.IdData.Type(typeId),
            goals = goals,
            runningRecord = runningRecord,
        )

        // Monthly
        check(
            goalRange = Range.Monthly,
            idData = RecordTypeGoal.IdData.Type(typeId),
            goals = goals,
            runningRecord = runningRecord,
        )
    }

    private suspend fun check(
        goalRange: Range,
        idData: RecordTypeGoal.IdData,
        goals: List<RecordTypeGoal>,
        runningRecord: RunningRecord,
    ) {
        val goal = goals.firstOrNull {
            it.isCorrectRange(goalRange) &&
                it.idData is RecordTypeGoal.IdData.Type &&
                it.type is RecordTypeGoal.Type.Duration
        }.value * 1000

        if (goal > 0) {
            val current = when (goalRange) {
                is Range.Session -> System.currentTimeMillis() - runningRecord.timeStarted
                is Range.Daily -> getCurrentRecordsDurationInteractor.getDailyCurrent(runningRecord).duration
                is Range.Weekly -> getCurrentRecordsDurationInteractor.getWeeklyCurrent(runningRecord).duration
                is Range.Monthly -> getCurrentRecordsDurationInteractor.getMonthlyCurrent(runningRecord).duration
            }

            if (goal > current) {
                scheduler.schedule(
                    durationMillisFromNow = goal - current,
                    idData = idData,
                    goalRange = goalRange,
                )
            }
        }
    }

    private fun RecordTypeGoal.isCorrectRange(range: Range): Boolean {
        return when (range) {
            is Range.Session -> this.range is Range.Session
            is Range.Daily -> this.range is Range.Daily
            is Range.Weekly -> this.range is Range.Weekly
            is Range.Monthly -> this.range is Range.Monthly
        }
    }
}
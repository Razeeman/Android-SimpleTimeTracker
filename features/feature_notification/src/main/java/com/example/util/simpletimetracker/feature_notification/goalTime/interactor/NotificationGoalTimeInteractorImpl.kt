package com.example.util.simpletimetracker.feature_notification.goalTime.interactor

import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.value
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalRangeEndInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal.Range
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_notification.goalTime.manager.NotificationGoalTimeManager
import com.example.util.simpletimetracker.feature_notification.goalTime.scheduler.NotificationGoalTimeScheduler
import javax.inject.Inject

/**
 * Type goal can be changed:
 * - add / change / remove running record
 * - add / change / remove record
 * - add / change / remove type goal
 * - remove type
 *
 * Category goal can be changed:
 * - add / change / remove running record
 * - add / change / remove record
 * - add / change / remove category goal
 * - remove category
 * - change type categories
 * - change category activities
 */
class NotificationGoalTimeInteractorImpl @Inject constructor(
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val getCurrentRecordsDurationInteractor: GetCurrentRecordsDurationInteractor,
    private val manager: NotificationGoalTimeManager,
    private val scheduler: NotificationGoalTimeScheduler,
    private val notificationGoalRangeEndInteractor: NotificationGoalRangeEndInteractor,
    private val notificationGoalParamsInteractor: NotificationGoalParamsInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
) : NotificationGoalTimeInteractor {

    override suspend fun checkAndReschedule(typeIds: List<Long>) {
        val typeIdsToCheck = typeIds
            .takeUnless { it.isEmpty() }
            ?: runningRecordInteractor.getAll().map(RunningRecord::id)

        typeIdsToCheck.forEach { checkAndRescheduleType(it) }
        checkAndRescheduleCategory(typeIdsToCheck)
        notificationGoalRangeEndInteractor.checkAndReschedule()
    }

    override fun cancel(idData: RecordTypeGoal.IdData) {
        listOf(
            Range.Session,
            Range.Daily,
            Range.Weekly,
            Range.Monthly,
        ).forEach {
            scheduler.cancelSchedule(idData, it)
            manager.hide(idData, it)
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
        )?.let(manager::show)
    }

    private suspend fun checkAndRescheduleType(typeId: Long) {
        cancel(RecordTypeGoal.IdData.Type(typeId))

        val runningRecord = runningRecordInteractor.get(typeId) ?: return
        val goals = recordTypeGoalInteractor.getByType(typeId)

        // Session
        checkType(
            goalRange = Range.Session,
            idData = RecordTypeGoal.IdData.Type(typeId),
            goals = goals,
            runningRecord = runningRecord,
        )

        // Daily
        checkType(
            goalRange = Range.Daily,
            idData = RecordTypeGoal.IdData.Type(typeId),
            goals = goals,
            runningRecord = runningRecord,
        )

        // Weekly
        checkType(
            goalRange = Range.Weekly,
            idData = RecordTypeGoal.IdData.Type(typeId),
            goals = goals,
            runningRecord = runningRecord,
        )

        // Monthly
        checkType(
            goalRange = Range.Monthly,
            idData = RecordTypeGoal.IdData.Type(typeId),
            goals = goals,
            runningRecord = runningRecord,
        )
    }

    private suspend fun checkAndRescheduleCategory(typeIds: List<Long>) {
        // Find all category goals.
        val goals = recordTypeGoalInteractor.getAllCategoryGoals()
            .filter { it.type is RecordTypeGoal.Type.Duration }
        if (goals.isEmpty()) return

        // Find all categories that hold this types.
        val categories = recordTypeCategoryInteractor.getAll()
            .groupBy(RecordTypeCategory::categoryId)
        val categoriesWithThisTypes = categories
            .mapValues { it.value.map(RecordTypeCategory::recordTypeId) }
            .filterValues { it.any { typeId -> typeId in typeIds } }

        // If this types doesn't affect any categories - exit.
        if (categoriesWithThisTypes.isEmpty()) return

        // If affected categories doesn't have goals - exit.
        val affectedCategoryGoals = goals
            .filter { it.idData.value in categoriesWithThisTypes.keys }
        if (affectedCategoryGoals.isEmpty()) return

        // For each goal check current results.
        val runningRecords = runningRecordInteractor.getAll()

        categoriesWithThisTypes.keys.forEach { categoryId ->
            cancel(RecordTypeGoal.IdData.Category(categoryId))
        }

        // Session
        checkCategory(
            goalRange = Range.Session,
            goals = affectedCategoryGoals,
            runningRecords = runningRecords,
            categoriesWithThisTypes = categoriesWithThisTypes,
        )

        // Daily
        checkCategory(
            goalRange = Range.Daily,
            goals = affectedCategoryGoals,
            runningRecords = runningRecords,
            categoriesWithThisTypes = categoriesWithThisTypes,
        )

        // Weekly
        checkCategory(
            goalRange = Range.Weekly,
            goals = affectedCategoryGoals,
            runningRecords = runningRecords,
            categoriesWithThisTypes = categoriesWithThisTypes,
        )

        // Monthly
        checkCategory(
            goalRange = Range.Monthly,
            goals = affectedCategoryGoals,
            runningRecords = runningRecords,
            categoriesWithThisTypes = categoriesWithThisTypes,
        )
    }

    private suspend fun checkType(
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

    private suspend fun checkCategory(
        goalRange: Range,
        goals: List<RecordTypeGoal>,
        runningRecords: List<RunningRecord>,
        categoriesWithThisTypes: Map<Long, List<Long>>,
    ) {
        val rangeGoals = goals.filter {
            it.isCorrectRange(goalRange) &&
                it.idData is RecordTypeGoal.IdData.Category &&
                it.type is RecordTypeGoal.Type.Duration &&
                it.value > 0
        }
        if (rangeGoals.isEmpty()) return

        val allTypeIdsFromTheseCategories = categoriesWithThisTypes.values
            .flatten().toSet().toList()
        val allCurrents = if (goalRange is Range.Session) {
            allTypeIdsFromTheseCategories.associateWith { typeId ->
                runningRecords
                    .filter { it.id == typeId }
                    .sumOf { System.currentTimeMillis() - it.timeStarted }
            }
        } else {
            getCurrentRecordsDurationInteractor.getAllCurrents(
                typeIds = allTypeIdsFromTheseCategories,
                runningRecords = runningRecords,
                rangeLength = when (goalRange) {
                    is Range.Session -> return
                    is Range.Daily -> RangeLength.Day
                    is Range.Weekly -> RangeLength.Week
                    is Range.Monthly -> RangeLength.Month
                },
            ).mapValues {
                it.value.duration
            }
        }

        val thisRangeCurrents = categoriesWithThisTypes.mapNotNull { (categoryId, typeIds) ->
            val currents = allCurrents
                .filter { it.key in typeIds }
                .values
                .toList()
            categoryId to currents.sum()
        }.toMap()
        val thisRangeRunningCounts = categoriesWithThisTypes.mapNotNull { (categoryId, typeIds) ->
            val counts = runningRecords
                .filter { it.id in typeIds }
                .size
            categoryId to counts
        }.toMap()

        rangeGoals.forEach { goal ->
            val categoryId = (goal.idData as? RecordTypeGoal.IdData.Category)?.value
                ?: return@forEach
            val current = thisRangeCurrents[categoryId].orZero()
            val goalValue = goal.value * 1000
            if (goalValue > current) {
                val count = thisRangeRunningCounts[categoryId].orZero()
                    .takeUnless { it == 0 } ?: return@forEach
                scheduler.schedule(
                    durationMillisFromNow = (goalValue - current) / count,
                    idData = RecordTypeGoal.IdData.Category(categoryId),
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
package com.example.util.simpletimetracker.feature_notification.goalTime.interactor

import com.example.util.simpletimetracker.core.interactor.FilterGoalsByDayOfWeekInteractor
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.recordType.extension.value
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationGoalRangeEndInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.category.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.domain.category.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.notifications.interactor.ActivityStartedStoppedBroadcastInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal.Range
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordType.extension.toRangeLength
import com.example.util.simpletimetracker.feature_notification.goalTime.manager.NotificationGoalTimeManager
import com.example.util.simpletimetracker.feature_notification.goalTime.scheduler.NotificationGoalTimeScheduler
import javax.inject.Inject
import kotlin.collections.component1
import kotlin.collections.component2

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
 *
 * Tag goal can be changed:
 * - add / change / remove running record
 * - add / change / remove record
 * - add / change / remove tag goal
 * - remove tag
 * - change record tags
 * - change running record tags
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
    private val filterGoalsByDayOfWeekInteractor: FilterGoalsByDayOfWeekInteractor,
    private val activityStartedStoppedBroadcastInteractor: ActivityStartedStoppedBroadcastInteractor,
) : NotificationGoalTimeInteractor {

    override suspend fun checkAndReschedule(typeIds: List<Long>) {
        val typeIdsToCheck = typeIds
            .takeUnless { it.isEmpty() }
            ?: runningRecordInteractor.getAll().map(RunningRecord::id)

        typeIdsToCheck.forEach { checkAndRescheduleType(it) }
        checkAndRescheduleCategory(typeIdsToCheck)
        notificationGoalRangeEndInteractor.checkAndReschedule()
    }

    override suspend fun checkAndRescheduleTags(tagIds: List<Long>) {
        val tagIdsToCheck = tagIds
            .takeUnless { it.isEmpty() }
            ?: runningRecordInteractor.getAll().map(RunningRecord::tags)
                .flatten().map(RecordBase.Tag::tagId).distinct()

        checkAndRescheduleTag(tagIdsToCheck)

        // TODO TAG GOAL duplicated call from checkAndReschedule?
        notificationGoalRangeEndInteractor.checkAndReschedule()
    }

    override fun cancel(idData: RecordTypeGoal.IdData) {
        getAvailableRanges().forEach {
            scheduler.cancelSchedule(idData, it)
            manager.hide(idData, it)
        }
    }

    override suspend fun show(
        idData: RecordTypeGoal.IdData,
        goalRange: Range,
    ) {
        val params = notificationGoalParamsInteractor.execute(
            idData = idData,
            range = goalRange,
            type = NotificationGoalParamsInteractor.Type.Duration,
        )
        params?.let(manager::show)
        params?.let {
            activityStartedStoppedBroadcastInteractor.onGoalReached(
                idData = idData,
                goalType = it.goalType,
            )
        }
    }

    private suspend fun checkAndRescheduleType(typeId: Long) {
        cancel(RecordTypeGoal.IdData.Type(typeId))

        val runningRecord = runningRecordInteractor.get(typeId) ?: return
        val goals = filterGoalsByDayOfWeekInteractor
            .execute(recordTypeGoalInteractor.getByType(typeId))
            .filter { it.type is RecordTypeGoal.Type.Duration }

        if (goals.isEmpty()) return

        getAvailableRanges().forEach { range ->
            checkType(
                goalRange = range,
                idData = RecordTypeGoal.IdData.Type(typeId),
                goals = goals,
                runningRecord = runningRecord,
            )
        }
    }

    private suspend fun checkAndRescheduleCategory(typeIds: List<Long>) {
        // Find all category goals.
        val goals = filterGoalsByDayOfWeekInteractor
            .execute(recordTypeGoalInteractor.getAllCategoryGoals())
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

        getAvailableRanges().forEach { range ->
            checkCategory(
                goalRange = range,
                goals = affectedCategoryGoals,
                runningRecords = runningRecords,
                categoriesWithThisTypes = categoriesWithThisTypes,
            )
        }
    }

    private suspend fun checkAndRescheduleTag(tagIds: List<Long>) {
        // Find all tag goals.
        val goals = filterGoalsByDayOfWeekInteractor
            .execute(recordTypeGoalInteractor.getAllTagGoals())
            .filter { it.type is RecordTypeGoal.Type.Duration }

        if (goals.isEmpty()) return

        // If doesn't affect any tags - exit.
        if (tagIds.isEmpty()) return

        // If affected tags doesn't have goals - exit.
        val affectedTagGoals = goals
            .filter { it.idData.value in tagIds }
        if (affectedTagGoals.isEmpty()) return

        // For each goal check current results.
        val runningRecords = runningRecordInteractor.getAll()

        tagIds.forEach { tagId ->
            cancel(RecordTypeGoal.IdData.Tag(tagId))
        }

        getAvailableRanges().forEach { range ->
            checkTag(
                goalRange = range,
                goals = affectedTagGoals,
                runningRecords = runningRecords,
                tagIds = tagIds,
            )
        }
    }

    private suspend fun checkType(
        goalRange: Range,
        idData: RecordTypeGoal.IdData,
        goals: List<RecordTypeGoal>,
        runningRecord: RunningRecord,
    ) {
        val rangeGoals = filterGoalsFromRange(goalRange, goals)
        val goal = rangeGoals.firstOrNull().value * 1000

        if (goal > 0) {
            val current = if (goalRange is Range.Session) {
                System.currentTimeMillis() - runningRecord.timeStarted
            } else {
                getCurrentRecordsDurationInteractor.getRangeCurrent(
                    typeId = runningRecord.id,
                    runningRecord = runningRecord,
                    rangeLength = goalRange.toRangeLength() ?: return,
                ).duration
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
        val rangeGoals = filterGoalsFromRange(goalRange, goals)
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

        val thisRangeCurrents = categoriesWithThisTypes.map { (categoryId, typeIds) ->
            val currents = allCurrents.filter { it.key in typeIds }.values.toList()
            categoryId to currents.sum()
        }.toMap()
        val thisRangeRunningCounts = categoriesWithThisTypes.map { (categoryId, typeIds) ->
            val counts = runningRecords.filter { it.id in typeIds }.size
            categoryId to counts
        }.toMap()

        rangeGoals.forEach { goal ->
            val categoryId = goal.idData.value
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

    private suspend fun checkTag(
        goalRange: Range,
        goals: List<RecordTypeGoal>,
        runningRecords: List<RunningRecord>,
        tagIds: List<Long>,
    ) {
        val rangeGoals = filterGoalsFromRange(goalRange, goals)
        if (rangeGoals.isEmpty()) return

        val allCurrents = if (goalRange is Range.Session) {
            tagIds.associateWith { tagId ->
                runningRecords
                    .filter { record -> record.tags.any { it.tagId == tagId } }
                    .sumOf { System.currentTimeMillis() - it.timeStarted }
            }
        } else {
            getCurrentRecordsDurationInteractor.getAllTagCurrents(
                tagIds = tagIds,
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

        val thisRangeCurrents = tagIds.associateWith { tagId ->
            allCurrents[tagId]
        }
        val thisRangeRunningCounts = tagIds.associateWith { tagId ->
            runningRecords.filter { record -> record.tags.any { it.tagId == tagId } }.size
        }

        rangeGoals.forEach { goal ->
            val tagId = goal.idData.value
            val current = thisRangeCurrents[tagId].orZero()
            val goalValue = goal.value * 1000
            if (goalValue > current) {
                val count = thisRangeRunningCounts[tagId].orZero()
                    .takeUnless { it == 0 } ?: return@forEach
                scheduler.schedule(
                    durationMillisFromNow = (goalValue - current) / count,
                    idData = RecordTypeGoal.IdData.Tag(tagId),
                    goalRange = goalRange,
                )
            }
        }
    }

    private fun filterGoalsFromRange(
        goalRange: Range,
        goals: List<RecordTypeGoal>,
    ): List<RecordTypeGoal> {
        return goals.filter { it.isCorrectRange(goalRange) && it.value > 0 }
    }

    private fun RecordTypeGoal.isCorrectRange(range: Range): Boolean {
        return when (range) {
            is Range.Session -> this.range is Range.Session
            is Range.Daily -> this.range is Range.Daily
            is Range.Weekly -> this.range is Range.Weekly
            is Range.Monthly -> this.range is Range.Monthly
        }
    }

    private fun getAvailableRanges(): List<Range> {
        return listOf(Range.Session, Range.Daily, Range.Weekly, Range.Monthly)
    }
}
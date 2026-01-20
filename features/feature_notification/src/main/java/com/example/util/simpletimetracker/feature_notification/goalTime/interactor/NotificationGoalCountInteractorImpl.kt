package com.example.util.simpletimetracker.feature_notification.goalTime.interactor

import com.example.util.simpletimetracker.core.interactor.FilterGoalsByDayOfWeekInteractor
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.recordType.extension.value
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationGoalCountInteractor
import com.example.util.simpletimetracker.domain.category.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.category.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.notifications.interactor.ActivityStartedStoppedBroadcastInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal.Range
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal.Type
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordType.extension.toRangeLength
import com.example.util.simpletimetracker.feature_notification.goalTime.manager.NotificationGoalTimeManager
import javax.inject.Inject

class NotificationGoalCountInteractorImpl @Inject constructor(
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val getCurrentRecordsDurationInteractor: GetCurrentRecordsDurationInteractor,
    private val manager: NotificationGoalTimeManager,
    private val notificationGoalParamsInteractor: NotificationGoalParamsInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val filterGoalsByDayOfWeekInteractor: FilterGoalsByDayOfWeekInteractor,
    private val activityStartedStoppedBroadcastInteractor: ActivityStartedStoppedBroadcastInteractor,
) : NotificationGoalCountInteractor {

    override suspend fun checkAndShow(typeId: Long) {
        checkAndShowType(typeId)
        checkAndShowCategory(typeId)
        checkAndShowTag(typeId)
    }

    private suspend fun checkAndShowType(typeId: Long) {
        val runningRecord = runningRecordInteractor.get(typeId) ?: return
        val goals = filterGoalsByDayOfWeekInteractor
            .execute(recordTypeGoalInteractor.getByType(typeId))
            .filter { it.type is Type.Count }

        // No count goals - exit.
        if (goals.isEmpty()) return

        getAllGoalRanges().forEach { goalRange ->
            checkType(
                goalRange = goalRange,
                goals = goals,
                typeId = typeId,
                runningRecord = runningRecord,
            )
        }
    }

    private suspend fun checkAndShowCategory(typeId: Long) {
        // Find all category goals.
        val goals = filterGoalsByDayOfWeekInteractor
            .execute(recordTypeGoalInteractor.getAllCategoryGoals())
            .filter { it.type is Type.Count }

        // No count goals - exit.
        if (goals.isEmpty()) return

        // Find all categories that hold this type.
        val categories = recordTypeCategoryInteractor.getAll()
            .groupBy(RecordTypeCategory::categoryId)
        val categoriesWithThisType = categories
            .mapValues { it.value.map(RecordTypeCategory::recordTypeId) }
            .filterValues { typeId in it }

        // If this type doesn't affect any categories - exit.
        if (categoriesWithThisType.isEmpty()) return

        // If affected categories doesn't have goals - exit.
        val affectedCategoryGoals = goals
            .filter { it.idData.value in categoriesWithThisType.keys }
        if (affectedCategoryGoals.isEmpty()) return

        // For each goal check current results.
        val runningRecords = runningRecordInteractor.getAll()

        getAllGoalRanges().forEach { goalRange ->
            checkCategory(
                goalRange = goalRange,
                goals = affectedCategoryGoals,
                runningRecords = runningRecords,
                categoriesWithThisType = categoriesWithThisType,
            )
        }
    }

    private suspend fun checkAndShowTag(typeId: Long) {
        val runningRecord = runningRecordInteractor.get(typeId) ?: return
        // Find all tag goals.
        val goals = filterGoalsByDayOfWeekInteractor
            .execute(recordTypeGoalInteractor.getAllTagGoals())
            .filter { it.type is Type.Count }

        // No count goals - exit.
        if (goals.isEmpty()) return

        // Find all tags that was started.
        val tags = runningRecord.tags.map { it.tagId }

        // If no tags started - exit.
        if (tags.isEmpty()) return

        // If affected tags doesn't have goals - exit.
        val affectedTagGoals = goals
            .filter { it.idData.value in tags }
        if (affectedTagGoals.isEmpty()) return

        // For each goal check current results.
        val runningRecords = runningRecordInteractor.getAll()

        getAllGoalRanges().forEach { goalRange ->
            checkTag(
                goalRange = goalRange,
                goals = affectedTagGoals,
                runningRecords = runningRecords,
            )
        }
    }

    private suspend fun checkType(
        goalRange: Range,
        goals: List<RecordTypeGoal>,
        typeId: Long,
        runningRecord: RunningRecord,
    ) {
        val goal = filterGoalsFromRange(goalRange, goals).firstOrNull()
        if (goal == null) return

        val current = getCurrentRecordsDurationInteractor.getRangeCurrent(
            typeId = runningRecord.id,
            runningRecord = runningRecord,
            rangeLength = goalRange.toRangeLength() ?: return,
        ).count

        if (current == goal.value) {
            show(
                idData = RecordTypeGoal.IdData.Type(typeId),
                goalRange = goalRange,
            )
        }
    }

    private suspend fun checkCategory(
        goalRange: Range,
        goals: List<RecordTypeGoal>,
        runningRecords: List<RunningRecord>,
        categoriesWithThisType: Map<Long, List<Long>>,
    ) {
        val rangeGoals = filterGoalsFromRange(goalRange, goals)
        if (rangeGoals.isEmpty()) return

        val allCurrents = getCurrentRecordsDurationInteractor.getAllCategoryCurrents(
            recordTypeCategories = categoriesWithThisType,
            runningRecords = runningRecords,
            rangeLength = goalRange.toRangeLength() ?: return,
        )

        rangeGoals.forEach { goal ->
            val categoryId = goal.idData.value
            val current = allCurrents[categoryId]?.count.orZero()
            if (current == goal.value) {
                show(
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
    ) {
        val rangeGoals = filterGoalsFromRange(goalRange, goals)
        if (rangeGoals.isEmpty()) return

        val allTags = runningRecords.flatMap(RunningRecord::tags)
            .map(RecordBase.Tag::tagId).distinct()
        val allCurrents = getCurrentRecordsDurationInteractor.getAllTagCurrents(
            tagIds = allTags,
            runningRecords = runningRecords,
            rangeLength = goalRange.toRangeLength() ?: return,
        )

        rangeGoals.forEach { goal ->
            val tagId = goal.idData.value
            val current = allCurrents[tagId]?.count.orZero()
            if (current == goal.value) {
                show(
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
        return goals.filter { it.isCorrectRange(goalRange) && it.value > 1 }
    }

    private fun RecordTypeGoal.isCorrectRange(range: Range): Boolean {
        return when (range) {
            is Range.Session -> false
            is Range.Daily -> this.range is Range.Daily
            is Range.Weekly -> this.range is Range.Weekly
            is Range.Monthly -> this.range is Range.Monthly
        }
    }

    private suspend fun show(idData: RecordTypeGoal.IdData, goalRange: Range) {
        val params = notificationGoalParamsInteractor.execute(
            idData = idData,
            range = goalRange,
            type = NotificationGoalParamsInteractor.Type.Count,
        )
        params?.let(manager::show)
        params?.let {
            activityStartedStoppedBroadcastInteractor.onGoalReached(
                idData = idData,
                goalType = it.goalType,
            )
        }
    }

    private fun getAllGoalRanges(): List<Range> {
        return listOf(Range.Daily, Range.Weekly, Range.Monthly)
    }
}
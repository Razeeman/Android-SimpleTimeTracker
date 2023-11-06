package com.example.util.simpletimetracker.feature_notification.goalTime.interactor

import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.domain.extension.value
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalCountInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal.Range
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal.Type
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_notification.goalTime.manager.NotificationGoalTimeManager
import javax.inject.Inject

class NotificationGoalCountInteractorImpl @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val getCurrentRecordsDurationInteractor: GetCurrentRecordsDurationInteractor,
    private val manager: NotificationGoalTimeManager,
    private val notificationGoalParamsInteractor: NotificationGoalParamsInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
) : NotificationGoalCountInteractor {

    override suspend fun checkAndShow(typeId: Long) {
        checkAndShowType(typeId)
        checkAndShowCategory(typeId)
    }

    override fun cancel(typeId: Long) {
        listOf(
            Range.Session,
            Range.Daily,
            Range.Weekly,
            Range.Monthly,
        ).forEach {
            manager.hide(typeId, it)
        }
    }

    private suspend fun checkAndShowType(typeId: Long) {
        val runningRecord = runningRecordInteractor.get(typeId) ?: return
        val goals = recordTypeGoalInteractor.getByType(typeId)
        if (goals.isEmpty()) return

        cancel(typeId)

        // Daily
        checkType(
            goalRange = Range.Daily,
            goals = goals,
            typeId = typeId,
            runningRecord = runningRecord,
        )

        // Weekly
        checkType(
            goalRange = Range.Weekly,
            goals = goals,
            typeId = typeId,
            runningRecord = runningRecord,
        )

        // Monthly
        checkType(
            goalRange = Range.Monthly,
            goals = goals,
            typeId = typeId,
            runningRecord = runningRecord,
        )
    }

    private suspend fun checkAndShowCategory(typeId: Long) {
        // Find all categories that hold this type.
        val categories = recordTypeCategoryInteractor.getAll()
            .groupBy(RecordTypeCategory::categoryId)
        val categoriesWithThisType = categories
            .mapValues { it.value.map(RecordTypeCategory::recordTypeId) }
            .filterValues { typeId in it }
        if (categoriesWithThisType.isEmpty()) return

        // Find all goals that set for these categories.
        val goals = recordTypeGoalInteractor.getByCategories(categoriesWithThisType.keys.toList())
        if (goals.isEmpty()) return

        // For each goal check current results.
        val runningRecords = runningRecordInteractor.getAll()
        val typesMap = recordTypeInteractor.getAll().associateBy(RecordType::id)

        // Daily
        checkCategory(
            goalRange = Range.Daily,
            goals = goals,
            typesMap = typesMap,
            runningRecords = runningRecords,
            categoriesWithThisType = categoriesWithThisType,
        )

        // Weekly
        checkCategory(
            goalRange = Range.Weekly,
            goals = goals,
            typesMap = typesMap,
            runningRecords = runningRecords,
            categoriesWithThisType = categoriesWithThisType,
        )

        // Monthly
        checkCategory(
            goalRange = Range.Monthly,
            goals = goals,
            typesMap = typesMap,
            runningRecords = runningRecords,
            categoriesWithThisType = categoriesWithThisType,
        )
    }

    private suspend fun checkType(
        goalRange: Range,
        goals: List<RecordTypeGoal>,
        typeId: Long,
        runningRecord: RunningRecord,
    ) {
        val goal = goals.firstOrNull {
            it.isCorrectRange(goalRange) &&
                it.idData is RecordTypeGoal.IdData.Type &&
                it.type is Type.Count
        }

        if (goal.value > 1) {
            val current =  when (goalRange) {
                is Range.Session -> return
                is Range.Daily -> getCurrentRecordsDurationInteractor.getDailyCurrent(runningRecord)
                is Range.Weekly -> getCurrentRecordsDurationInteractor.getWeeklyCurrent(runningRecord)
                is Range.Monthly -> getCurrentRecordsDurationInteractor.getMonthlyCurrent(runningRecord)
            }.count

            if (current == goal.value) {
                show(
                    idData = RecordTypeGoal.IdData.Type(typeId),
                    goalRange = goalRange,
                )
            }
        }
    }

    private suspend fun checkCategory(
        goalRange: Range,
        goals: List<RecordTypeGoal>,
        typesMap: Map<Long, RecordType>,
        runningRecords: List<RunningRecord>,
        categoriesWithThisType: Map<Long, List<Long>>,
    ) {
        val rangeGoals = goals.filter {
            it.isCorrectRange(goalRange) &&
                it.idData is RecordTypeGoal.IdData.Category &&
                it.type is Type.Count &&
                it.value > 1
        }
        if (rangeGoals.isEmpty()) return

        val allTypeIdsFromTheseCategories = categoriesWithThisType.values.flatten().toSet()
        val allCurrents = getCurrentRecordsDurationInteractor.getAllCurrents(
            typesMap = typesMap.filterKeys { it in allTypeIdsFromTheseCategories },
            runningRecords = runningRecords,
            rangeLength = when (goalRange) {
                is Range.Session -> return
                is Range.Daily -> RangeLength.Day
                is Range.Weekly -> RangeLength.Week
                is Range.Monthly -> RangeLength.Month
            },
        )

        val thisRangeCurrents = categoriesWithThisType.mapNotNull { (categoryId, typeIds) ->
            val currents = allCurrents
                .filter { it.key in typeIds }
                .values
                .toList()
            val current = GetCurrentRecordsDurationInteractor.Result(
                range = allCurrents.values.firstOrNull()?.range ?: return@mapNotNull null,
                duration = currents.sumOf { it.duration },
                count = currents.sumOf { it.count },
                durationDiffersFromCurrent = currents.any { it.durationDiffersFromCurrent },
            )
            categoryId to current
        }.toMap()

        rangeGoals.forEach { goal ->
            val categoryId = (goal.idData as? RecordTypeGoal.IdData.Category)?.value
                ?: return@forEach
            val current = thisRangeCurrents[categoryId]?.count
                ?: return@forEach
            if (current == goal.value) {
                show(
                    idData = RecordTypeGoal.IdData.Category(categoryId),
                    goalRange = goalRange,
                )
            }
        }
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
        notificationGoalParamsInteractor.execute(
            idData = idData,
            range = goalRange,
            type = NotificationGoalParamsInteractor.Type.Count,
        ).let(manager::show)
    }
}
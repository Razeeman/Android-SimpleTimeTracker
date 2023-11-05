package com.example.util.simpletimetracker.feature_notification.goalTime.interactor

import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.domain.extension.getDailyCount
import com.example.util.simpletimetracker.domain.extension.getMonthlyCount
import com.example.util.simpletimetracker.domain.extension.getWeeklyCount
import com.example.util.simpletimetracker.domain.extension.value
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalCountInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
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
            RecordTypeGoal.Range.Session,
            RecordTypeGoal.Range.Daily,
            RecordTypeGoal.Range.Weekly,
            RecordTypeGoal.Range.Monthly,
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
        val dailyGoalCount = goals.getDailyCount().value
        if (dailyGoalCount > 1) {
            val dailyCurrent = getCurrentRecordsDurationInteractor.getDailyCurrent(runningRecord)

            if (dailyGoalCount == dailyCurrent.count) {
                show(
                    idData = RecordTypeGoal.IdData.Type(typeId),
                    goalRange = RecordTypeGoal.Range.Daily,
                )
            }
        }

        // Weekly
        val weeklyGoalCount = goals.getWeeklyCount().value
        if (weeklyGoalCount > 1) {
            val weeklyCurrent = getCurrentRecordsDurationInteractor.getWeeklyCurrent(runningRecord)

            if (weeklyGoalCount == weeklyCurrent.count) {
                show(
                    idData = RecordTypeGoal.IdData.Type(typeId),
                    goalRange = RecordTypeGoal.Range.Weekly,
                )
            }
        }

        // Monthly
        val monthlyGoalCount = goals.getMonthlyCount().value
        if (monthlyGoalCount > 1) {
            val monthlyCurrent = getCurrentRecordsDurationInteractor.getMonthlyCurrent(runningRecord)

            if (monthlyGoalCount == monthlyCurrent.count) {
                show(
                    idData = RecordTypeGoal.IdData.Type(typeId),
                    goalRange = RecordTypeGoal.Range.Monthly,
                )
            }
        }
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
        val dailyGoals = goals.filter {
            it.range is RecordTypeGoal.Range.Daily &&
                it.type is RecordTypeGoal.Type.Count &&
                it.value > 1
        }
        if (dailyGoals.isNotEmpty()) {
            val allDailyCurrents = getCurrentRecordsDurationInteractor.getAllDailyCurrents(
                typesMap = typesMap,
                runningRecords = runningRecords,
            )

            val dailyCurrents = categoriesWithThisType.mapNotNull { (categoryId, typeIds) ->
                val dailyCurrents = allDailyCurrents
                    .filter { it.key in typeIds }
                    .values
                    .toList()
                val dailyCurrent = GetCurrentRecordsDurationInteractor.Result(
                    range = allDailyCurrents.values.firstOrNull()?.range ?: return@mapNotNull null,
                    duration = dailyCurrents.sumOf { it.duration },
                    count = dailyCurrents.sumOf { it.count },
                    durationDiffersFromCurrent = dailyCurrents.any { it.durationDiffersFromCurrent },
                )
                categoryId to dailyCurrent
            }.toMap()

            dailyGoals.forEach { goal ->
                val categoryId = (goal.idData as? RecordTypeGoal.IdData.Category)?.value
                    ?: return@forEach
                val current = dailyCurrents[categoryId]?.count
                    ?: return
                if (current == goal.value) {
                    show(
                        idData = RecordTypeGoal.IdData.Category(categoryId),
                        goalRange = RecordTypeGoal.Range.Daily,
                    )
                }
            }
        }
    }

    private suspend fun show(idData: RecordTypeGoal.IdData, goalRange: RecordTypeGoal.Range) {
        notificationGoalParamsInteractor.execute(
            idData = idData,
            range = goalRange,
            type = NotificationGoalParamsInteractor.Type.Count,
        ).let(manager::show)
    }
}
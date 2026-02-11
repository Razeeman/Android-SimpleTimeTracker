package com.example.util.simpletimetracker.domain.complexRule.interactor

import com.example.util.simpletimetracker.domain.daysOfWeek.interactor.GetCurrentDayInteractor
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.base.ResultContainer
import com.example.util.simpletimetracker.domain.complexRule.model.ComplexRule
import javax.inject.Inject

class ComplexRuleProcessActionInteractor @Inject constructor(
    private val complexRuleInteractor: ComplexRuleInteractor,
    private val getCurrentDayInteractor: GetCurrentDayInteractor,
) {

    suspend fun hasRules(): Boolean {
        return !complexRuleInteractor.isEmpty()
    }

    suspend fun processRules(
        timeStarted: Long,
        startingTypeId: Long,
        currentTypeIds: Set<Long>,
    ): Result {
        val rules = filterRulesByConditions(
            rules = complexRuleInteractor.getAll().filterNot { it.disabled },
            startingTypeId = startingTypeId,
            currentTypeIds = currentTypeIds,
            currentDay = getCurrentDayInteractor.execute(timeStarted),
        )
        val rulesThatAllow = rules
            .filter { it.action is ComplexRule.Action.AllowMultitasking }
        val rulesThatDisallow = rules
            .filter { it.action is ComplexRule.Action.DisallowMultitasking }
        val assignTagRules = rules
            .filter { it.action is ComplexRule.Action.AssignTag }

        val isMultitaskingAllowed = when {
            rulesThatAllow.isNotEmpty() -> ResultContainer.Defined(true)
            rulesThatDisallow.isNotEmpty() -> ResultContainer.Defined(false)
            else -> ResultContainer.Undefined
        }

        val disallowOnlyPreviousTypeIds = rulesThatDisallow
            .filter { it.actionDisallowOnlyPrevious }
            .map { it.conditionCurrentTypeIds }
            .flatten().toSet()
            .takeUnless { rulesThatDisallow.any { !it.actionDisallowOnlyPrevious } }
            .orEmpty()

        val additionalTags = assignTagRules.map { it.actionAssignTagIds }
            .flatten().toSet()

        return Result(
            isMultitaskingAllowed = isMultitaskingAllowed,
            disallowOnlyPreviousTypeIds = disallowOnlyPreviousTypeIds,
            tagsIds = additionalTags,
        )
    }

    private fun filterRulesByConditions(
        rules: List<ComplexRule>,
        startingTypeId: Long,
        currentTypeIds: Set<Long>,
        currentDay: DayOfWeek,
    ): List<ComplexRule> {
        return rules.filter { rule ->
            if (!rule.hasConditions) return@filter false

            rule.conditions.all { condition ->
                when (condition) {
                    is ComplexRule.Condition.StartingType ->
                        startingTypeId in rule.conditionStartingTypeIds
                    is ComplexRule.Condition.CurrentType ->
                        currentTypeIds.any { it in rule.conditionCurrentTypeIds }
                    is ComplexRule.Condition.DaysOfWeek ->
                        currentDay in rule.conditionDaysOfWeek
                }
            }
        }
    }

    data class Result(
        val isMultitaskingAllowed: ResultContainer<Boolean>,
        val disallowOnlyPreviousTypeIds: Set<Long>,
        val tagsIds: Set<Long>,
    )
}
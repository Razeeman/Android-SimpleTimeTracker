package com.example.util.simpletimetracker.data_local.complexRule

import com.example.util.simpletimetracker.domain.complexRule.model.ComplexRule
import com.example.util.simpletimetracker.domain.daysOfWeek.mapper.DaysOfWeekDataLocalMapper
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import javax.inject.Inject

class ComplexRuleDataLocalMapper @Inject constructor(
    private val daysOfWeekDataLocalMapper: DaysOfWeekDataLocalMapper,
    private val complexRuleTagValuesMapper: ComplexRuleTagValuesMapper,
) {

    fun map(dbo: ComplexRuleDBO): ComplexRule {
        val assignTagIds = mapIds(dbo.actionSetTagIds)
        val parsedValues = complexRuleTagValuesMapper.parse(dbo.actionSetTagValues)
        val assignTagValues = parsedValues.tagsWithValues.associateBy { it.tagId }
        val assignTagValueOnStartIds = assignTagIds
            .filter { parsedValues.tagIdsToSelectValueOnStart.contains(it) }
            .toSet()

        return ComplexRule(
            id = dbo.id,
            disabled = dbo.disabled,
            action = mapActionType(dbo.action),
            actionDisallowOnlyPrevious = dbo.actionDisallowOnlyPrevious,
            actionAssignTagValues = assignTagIds.map {
                RecordBase.Tag(tagId = it, numericValue = assignTagValues[it]?.numericValue)
            },
            actionAssignTagValueOnStartIds = assignTagValueOnStartIds,
            conditionStartingTypeIds = mapIds(dbo.conditionStartingTypeIds),
            conditionCurrentTypeIds = mapIds(dbo.conditionCurrentTypeIds),
            conditionDaysOfWeek = daysOfWeekDataLocalMapper
                .mapDaysOfWeek(dbo.conditionDaysOfWeek).toSet(),
        )
    }

    fun map(domain: ComplexRule): ComplexRuleDBO {
        return ComplexRuleDBO(
            id = domain.id,
            disabled = domain.disabled,
            action = mapActionType(domain.action),
            actionDisallowOnlyPrevious = domain.actionDisallowOnlyPrevious,
            actionSetTagIds = mapIds(domain.actionAssignTagIds),
            actionSetTagValues = complexRuleTagValuesMapper.serialize(
                data = domain.actionAssignTagValues,
                tagIdsToSelectValueOnStart = domain.actionAssignTagValueOnStartIds,
            ),
            conditionStartingTypeIds = mapIds(domain.conditionStartingTypeIds),
            conditionCurrentTypeIds = mapIds(domain.conditionCurrentTypeIds),
            conditionDaysOfWeek = daysOfWeekDataLocalMapper
                .mapDaysOfWeek(domain.conditionDaysOfWeek),
        )
    }

    private fun mapIds(dbo: String): Set<Long> {
        return dbo.split(',').mapNotNull(String::toLongOrNull).toSet()
    }

    private fun mapIds(domain: Set<Long>): String {
        return domain.joinToString(separator = ",")
    }

    private fun mapActionType(
        dbo: Long,
    ): ComplexRule.Action {
        return when (dbo) {
            0L -> ComplexRule.Action.AllowMultitasking
            1L -> ComplexRule.Action.DisallowMultitasking
            2L -> ComplexRule.Action.AssignTag
            else -> ComplexRule.Action.AllowMultitasking
        }
    }

    private fun mapActionType(
        domain: ComplexRule.Action,
    ): Long {
        return when (domain) {
            is ComplexRule.Action.AllowMultitasking -> 0L
            is ComplexRule.Action.DisallowMultitasking -> 1L
            is ComplexRule.Action.AssignTag -> 2L
        }
    }
}
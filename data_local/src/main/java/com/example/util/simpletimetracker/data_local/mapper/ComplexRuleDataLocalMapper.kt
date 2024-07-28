package com.example.util.simpletimetracker.data_local.mapper

import com.example.util.simpletimetracker.data_local.model.ComplexRuleDBO
import com.example.util.simpletimetracker.domain.model.ComplexRule
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import javax.inject.Inject

class ComplexRuleDataLocalMapper @Inject constructor() {

    fun map(dbo: ComplexRuleDBO): ComplexRule {
        return ComplexRule(
            id = dbo.id,
            action = mapActionType(dbo.actionType),
            actionSetTagIds = mapIds(dbo.actionSetTags),
            conditionStartingTypeIds = mapIds(dbo.conditionStartingActivity),
            conditionCurrentTypeIds = mapIds(dbo.conditionCurrentActivity),
            conditionDaysOfWeek = mapDaysOfWeek(dbo.conditionDaysOfWeek),
        )
    }

    fun map(domain: ComplexRule): ComplexRuleDBO {
        return ComplexRuleDBO(
            id = domain.id,
            actionType = mapActionType(domain.action),
            actionSetTags = mapIds(domain.actionSetTagIds),
            conditionStartingActivity = mapIds(domain.conditionStartingTypeIds),
            conditionCurrentActivity = mapIds(domain.conditionCurrentTypeIds),
            conditionDaysOfWeek = mapDaysOfWeek(domain.conditionDaysOfWeek),
        )
    }

    private fun mapIds(dbo: String): Set<Long> {
        return dbo.split(',').mapNotNull(String::toLongOrNull).toSet()
    }

    private fun mapIds(domain: Set<Long>): String {
        return domain.joinToString(separator = ",")
    }

    private fun mapActionType(dbo: Long): ComplexRule.Action {
        return when (dbo) {
            0L -> ComplexRule.Action.AllowMultitasking
            1L -> ComplexRule.Action.DisallowMultitasking
            2L -> ComplexRule.Action.SetTag
            else -> ComplexRule.Action.AllowMultitasking
        }
    }

    private fun mapActionType(domain: ComplexRule.Action): Long {
        return when (domain) {
            is ComplexRule.Action.AllowMultitasking -> 0L
            is ComplexRule.Action.DisallowMultitasking -> 1L
            is ComplexRule.Action.SetTag -> 2L
        }
    }

    private fun mapDaysOfWeek(dbo: String): Set<DayOfWeek> {
        return dbo.split(',').mapNotNull(daysOfWeekMapReversed::get).toSet()
    }

    private fun mapDaysOfWeek(domain: Set<DayOfWeek>): String {
        return domain.mapNotNull(daysOfWeekMap::get).joinToString(separator = ",")
    }

    companion object {
        private val daysOfWeekMap = mapOf(
            DayOfWeek.SUNDAY to "SUNDAY",
            DayOfWeek.MONDAY to "MONDAY",
            DayOfWeek.TUESDAY to "TUESDAY",
            DayOfWeek.WEDNESDAY to "WEDNESDAY",
            DayOfWeek.THURSDAY to "THURSDAY",
            DayOfWeek.FRIDAY to "FRIDAY",
            DayOfWeek.SATURDAY to "SATURDAY",
        )
        val daysOfWeekMapReversed = daysOfWeekMap.entries
            .associate { (key, value) -> value to key }
    }
}
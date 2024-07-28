package com.example.util.simpletimetracker.feature_complex_rules.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.ComplexRule
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_complex_rules.R
import com.example.util.simpletimetracker.feature_complex_rules.adapter.ComplexRuleAddViewData
import com.example.util.simpletimetracker.feature_complex_rules.adapter.ComplexRuleViewData
import javax.inject.Inject

class ComplexRulesViewDataMapper @Inject constructor(
    private val timeMapper: TimeMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
) {

    fun mapAddItem(
        isDarkTheme: Boolean,
    ): ComplexRuleAddViewData {
        return ComplexRuleAddViewData(
            name = resourceRepo.getString(R.string.running_records_add_type),
            color = colorMapper.toInactiveColor(isDarkTheme),
        )
    }

    fun mapRule(
        rule: ComplexRule,
        isDarkTheme: Boolean,
        typesMap: Map<Long, RecordType>,
        tagsMap: Map<Long, RecordTag>,
    ): ComplexRuleViewData {
        return ComplexRuleViewData(
            id = rule.id,
            actionTitle = mapActionTitle(rule, tagsMap),
            startingTypes = mapStartingTypes(rule, typesMap),
            currentTypes = mapCurrentTypes(rule, typesMap),
            daysOfWeek = mapDaysOfWeek(rule),
            color = colorMapper.toInactiveColor(isDarkTheme),
        )
    }

    private fun mapActionTitle(
        rule: ComplexRule,
        tagsMap: Map<Long, RecordTag>,
    ): String {
        val action = rule.action
        return when (action) {
            ComplexRule.Action.AllowMultitasking -> {
                resourceRepo.getString(R.string.settings_allow_multitasking)
            }
            ComplexRule.Action.DisallowMultitasking -> {
                resourceRepo.getString(R.string.settings_disallow_multitasking)
            }
            ComplexRule.Action.SetTag -> getFinalText(
                baseTitle = resourceRepo.getString(R.string.change_complex_action_set_tag),
                data = rule.actionSetTagIds.mapNotNull { tagsMap[it]?.name },
            )
        }
    }

    private fun mapStartingTypes(
        rule: ComplexRule,
        typesMap: Map<Long, RecordType>,
    ): String {
        return getFinalText(
            baseTitle = resourceRepo.getString(R.string.change_complex_starting_activity),
            data = rule.conditionStartingTypeIds.mapNotNull { typesMap[it]?.name },
        )
    }

    private fun mapCurrentTypes(
        rule: ComplexRule,
        typesMap: Map<Long, RecordType>,
    ): String {
        return getFinalText(
            baseTitle = resourceRepo.getString(R.string.change_complex_previous_activity),
            data = rule.conditionCurrentTypeIds.mapNotNull { typesMap[it]?.name },
        )
    }

    private fun mapDaysOfWeek(
        rule: ComplexRule,
    ): String {
        return getFinalText(
            baseTitle = resourceRepo.getString(R.string.range_day),
            data = rule.conditionDaysOfWeek.map { timeMapper.toShortDayOfWeekName(it) },
        )
    }

    private fun getFinalText(
        baseTitle: String,
        data: List<String>,
    ): String {
        return if (data.isNotEmpty()) {
            "$baseTitle : ${data.joinToString(separator = ", ")}"
        } else {
            ""
        }
    }
}
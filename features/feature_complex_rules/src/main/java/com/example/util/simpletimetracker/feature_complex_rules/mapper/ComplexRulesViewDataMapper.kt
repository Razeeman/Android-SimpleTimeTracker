package com.example.util.simpletimetracker.feature_complex_rules.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTagViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.ComplexRule
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_complex_rules.R
import com.example.util.simpletimetracker.feature_complex_rules.adapter.ComplexRuleAddViewData
import com.example.util.simpletimetracker.feature_complex_rules.adapter.ComplexRuleElementContentViewData
import com.example.util.simpletimetracker.feature_complex_rules.adapter.ComplexRuleElementTitleViewData
import com.example.util.simpletimetracker.feature_complex_rules.adapter.ComplexRuleViewData
import javax.inject.Inject

class ComplexRulesViewDataMapper @Inject constructor(
    private val timeMapper: TimeMapper,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val recordTagViewDataMapper: RecordTagViewDataMapper,
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
        typesOrder: List<Long>,
        tagsOrder: List<Long>,
    ): ComplexRuleViewData {
        val actionItems = mapActions(
            rule = rule,
            isDarkTheme = isDarkTheme,
            typesMap = typesMap,
            tagsMap = tagsMap,
            tagsOrder = tagsOrder,
        )
        val conditionItems = mapConditions(
            rule = rule,
            isDarkTheme = isDarkTheme,
            typesMap = typesMap,
            typesOrder = typesOrder,
        )

        return ComplexRuleViewData(
            id = rule.id,
            actionItems = actionItems,
            conditionItems = conditionItems,
            color = if (rule.disabled) {
                colorMapper.toInactiveColor(isDarkTheme)
            } else {
                colorMapper.toActiveColor(isDarkTheme)
            },
            disableButtonColor = if (rule.disabled) {
                colorMapper.toActiveColor(isDarkTheme)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            },
            disableButtonText = if (rule.disabled) {
                R.string.complex_rules_enable
            } else {
                R.string.complex_rules_disable
            }.let(resourceRepo::getString),
        )
    }

    private fun mapActions(
        rule: ComplexRule,
        isDarkTheme: Boolean,
        typesMap: Map<Long, RecordType>,
        tagsMap: Map<Long, RecordTag>,
        tagsOrder: List<Long>,
    ): List<ViewHolderType> {
        val action = rule.action
        val data = when (action) {
            is ComplexRule.Action.AllowMultitasking,
            is ComplexRule.Action.DisallowMultitasking,
            -> emptyList()
            is ComplexRule.Action.AssignTag -> rule.actionAssignTagIds
                .sortedBy { tagsOrder.indexOf(it) }
                .mapNotNull { tagsMap[it] }
        }
        val result = mutableListOf<ViewHolderType>()
        result += ComplexRuleElementTitleViewData(
            text = mapActionTitle(action),
        )
        result += data.map {
            ComplexRuleElementContentViewData(
                text = it.name,
                icon = recordTagViewDataMapper.mapIcon(
                    tag = it,
                    type = typesMap[it.iconColorSource],
                )?.let(iconMapper::mapIcon),
                color = colorMapper.mapToColorInt(
                    color = it.color,
                    isDarkTheme = isDarkTheme,
                ),
            )
        }
        return result
    }

    private fun mapConditions(
        rule: ComplexRule,
        isDarkTheme: Boolean,
        typesMap: Map<Long, RecordType>,
        typesOrder: List<Long>,
    ): List<ViewHolderType> {
        val conditionItems = mutableListOf<ViewHolderType>()
        conditionItems += mapTypes(
            typeIds = rule.conditionStartingTypeIds,
            title = resourceRepo.getString(R.string.change_complex_starting_activity),
            isDarkTheme = isDarkTheme,
            typesMap = typesMap,
            typesOrder = typesOrder,
        )
        conditionItems += mapTypes(
            typeIds = rule.conditionCurrentTypeIds,
            title = resourceRepo.getString(R.string.change_complex_previous_activity),
            isDarkTheme = isDarkTheme,
            typesMap = typesMap,
            typesOrder = typesOrder,
        )
        conditionItems += mapDaysOfWeek(rule)
        return conditionItems
    }

    fun mapActionTitle(
        action: ComplexRule.Action,
    ): String {
        return when (action) {
            is ComplexRule.Action.AllowMultitasking ->
                R.string.settings_allow_multitasking
            is ComplexRule.Action.DisallowMultitasking ->
                R.string.settings_disallow_multitasking
            is ComplexRule.Action.AssignTag ->
                R.string.change_complex_action_assign_tag
        }.let(resourceRepo::getString)
    }

    private fun mapTypes(
        typeIds: Set<Long>,
        title: String,
        isDarkTheme: Boolean,
        typesMap: Map<Long, RecordType>,
        typesOrder: List<Long>,
    ): List<ViewHolderType> {
        val data = typeIds
            .sortedBy { typesOrder.indexOf(it) }
            .mapNotNull { typesMap[it] }
        if (data.isEmpty()) return emptyList()

        val result = mutableListOf<ViewHolderType>()
        result += ComplexRuleElementTitleViewData(title)
        result += data.map {
            ComplexRuleElementContentViewData(
                text = it.name,
                icon = iconMapper.mapIcon(it.icon),
                color = colorMapper.mapToColorInt(
                    color = it.color,
                    isDarkTheme = isDarkTheme,
                ),
            )
        }
        return result
    }

    private fun mapDaysOfWeek(
        rule: ComplexRule,
    ): List<ViewHolderType> {
        val data = rule.conditionDaysOfWeek
            .map { timeMapper.toShortDayOfWeekName(it) }
        if (data.isEmpty()) return emptyList()

        val result = mutableListOf<ViewHolderType>()
        result += ComplexRuleElementTitleViewData(
            text = resourceRepo.getString(R.string.range_day),
        )
        result += data.map {
            ComplexRuleElementContentViewData(
                text = it,
                icon = null,
                color = resourceRepo.getColor(R.color.colorSecondary),
            )
        }
        return result
    }
}
package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.complexRule.model.ComplexRule
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.complexRule.ComplexRuleElementTitleViewData
import com.example.util.simpletimetracker.feature_base_adapter.complexRule.ComplexRuleViewData
import com.example.util.simpletimetracker.feature_base_adapter.listElement.ListElementViewData
import javax.inject.Inject

class ComplexRuleViewDataMapper @Inject constructor(
    private val timeMapper: TimeMapper,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val recordTagViewDataMapper: RecordTagViewDataMapper,
) {

    fun mapRule(
        rule: ComplexRule,
        isDarkTheme: Boolean,
        typesMap: Map<Long, RecordType>,
        tagsMap: Map<Long, RecordTag>,
        typesOrder: List<Long>,
        tagsOrder: List<Long>,
    ): ComplexRuleViewData {
        return mapRuleFiltered(
            rule = rule,
            isDarkTheme = isDarkTheme,
            typesMap = typesMap,
            tagsMap = tagsMap,
            typesOrder = typesOrder,
            tagsOrder = tagsOrder,
            isFiltered = rule.disabled,
            disableButtonVisible = true,
        )
    }

    fun mapRuleFiltered(
        rule: ComplexRule,
        isDarkTheme: Boolean,
        typesMap: Map<Long, RecordType>,
        tagsMap: Map<Long, RecordTag>,
        typesOrder: List<Long>,
        tagsOrder: List<Long>,
        isFiltered: Boolean,
        disableButtonVisible: Boolean,
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
            color = if (isFiltered) {
                colorMapper.toInactiveColor(isDarkTheme)
            } else {
                colorMapper.toActiveColor(isDarkTheme)
            },
            disableButtonColor = if (isFiltered) {
                colorMapper.toActiveColor(isDarkTheme)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            },
            disableButtonText = if (isFiltered) {
                R.string.complex_rules_enable
            } else {
                R.string.complex_rules_disable
            }.let(resourceRepo::getString),
            disableButtonVisible = disableButtonVisible,
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
            is ComplexRule.Action.AssignTag -> {
                rule.actionAssignTagIds
                    .sortedBy { tagsOrder.indexOf(it) }
                    .mapNotNull { tagsMap[it] }
            }
        }
        val result = mutableListOf<ViewHolderType>()
        result += ComplexRuleElementTitleViewData(
            text = mapActionTitle(action),
        )
        result += data.map {
            ListElementViewData(
                text = it.name,
                icon = recordTagViewDataMapper.mapIcon(
                    tag = it,
                    types = typesMap,
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
            ListElementViewData(
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
            ListElementViewData(
                text = it,
                icon = null,
                color = resourceRepo.getColor(R.color.colorSecondary),
            )
        }
        return result
    }
}
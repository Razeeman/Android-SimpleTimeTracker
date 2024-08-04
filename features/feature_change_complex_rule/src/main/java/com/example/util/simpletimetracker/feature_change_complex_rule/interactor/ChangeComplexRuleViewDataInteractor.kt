package com.example.util.simpletimetracker.feature_change_complex_rule.interactor

import com.example.util.simpletimetracker.core.mapper.CommonViewDataMapper
import com.example.util.simpletimetracker.core.mapper.DayOfWeekViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.ComplexRule
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_change_complex_rule.R
import com.example.util.simpletimetracker.feature_change_complex_rule.adapter.ChangeComplexRuleActionViewData
import com.example.util.simpletimetracker.feature_change_complex_rule.mapper.ChangeComplexRuleViewDataMapper
import com.example.util.simpletimetracker.feature_change_complex_rule.viewData.ChangeComplexRuleActionChooserViewData
import com.example.util.simpletimetracker.feature_change_complex_rule.viewData.ChangeComplexRuleTypesChooserViewData
import javax.inject.Inject

class ChangeComplexRuleViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val dayOfWeekViewDataMapper: DayOfWeekViewDataMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val changeComplexRuleViewDataMapper: ChangeComplexRuleViewDataMapper,
    private val commonViewDataMapper: CommonViewDataMapper,
) {

    fun getActionViewData(
        newActionType: ComplexRule.Action?,
        newAssignTagIds: Set<Long>,
    ): ChangeComplexRuleActionChooserViewData {
        val hint = HintViewData(resourceRepo.getString(R.string.change_complex_actions_hint))
        val items = listOf(
            ComplexRule.Action.AllowMultitasking,
            ComplexRule.Action.DisallowMultitasking,
            ComplexRule.Action.AssignTag,
        ).map {
            ChangeComplexRuleActionViewData(
                type = changeComplexRuleViewDataMapper.mapAction(it),
                text = changeComplexRuleViewDataMapper.mapActionTitle(it, newAssignTagIds),
            )
        }
        val selectedCount = if (newActionType is ComplexRule.Action.AssignTag) {
            newAssignTagIds.size
        } else {
            0
        }

        return ChangeComplexRuleActionChooserViewData(
            title = changeComplexRuleViewDataMapper.mapActionTitle(newActionType, newAssignTagIds),
            selectedCount = selectedCount,
            viewData = listOf(hint) + items,
        )
    }

    suspend fun getTypesViewData(
        selectedIds: Set<Long>,
        originalSelectedIds: Set<Long>,
    ): ChangeComplexRuleTypesChooserViewData {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val data = recordTypeInteractor.getAll()
            .filter { !it.hidden || it.id in originalSelectedIds }
            .map {
                it.id to recordTypeViewDataMapper.map(
                    recordType = it,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme,
                    isChecked = null,
                )
            }

        return if (data.isNotEmpty()) {
            val selected = data.filter { it.first in selectedIds }.map { it.second }
            val available = data.filter { it.first !in selectedIds }.map { it.second }
            val viewData = mutableListOf<ViewHolderType>()
            commonViewDataMapper.mapSelectedHint(
                isEmpty = selected.isEmpty(),
            ).let(viewData::add)
            selected.let(viewData::addAll)
            DividerViewData(1)
                .takeUnless { available.isEmpty() }
                ?.let(viewData::add)
            available.let(viewData::addAll)

            ChangeComplexRuleTypesChooserViewData(
                selectedCount = selected.size,
                viewData = viewData,
            )
        } else {
            ChangeComplexRuleTypesChooserViewData(
                selectedCount = 0,
                viewData = recordTypeViewDataMapper.mapToEmpty(),
            )
        }
    }

    suspend fun getDaysOfWeek(
        daysOfWeek: Set<DayOfWeek>,
    ): ChangeComplexRuleTypesChooserViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val viewData = dayOfWeekViewDataMapper.mapViewData(
            selectedDaysOfWeek = daysOfWeek.toList(),
            isDarkTheme = isDarkTheme,
            width = DayOfWeekViewData.Width.MatchParent,
            paddingHorizontalDp = 2,
        )
        return ChangeComplexRuleTypesChooserViewData(
            selectedCount = daysOfWeek.size,
            viewData = viewData,
        )
    }
}
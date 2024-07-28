package com.example.util.simpletimetracker.feature_change_complex_rule.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.ComplexRule
import com.example.util.simpletimetracker.feature_change_complex_rule.adapter.ChangeComplexRuleActionViewData
import javax.inject.Inject

class ChangeComplexRuleViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun mapAction(
        action: ComplexRule.Action,
    ): ChangeComplexRuleActionViewData.Type {
        return when (action) {
            ComplexRule.Action.AllowMultitasking ->
                ChangeComplexRuleActionViewData.Type.AllowMultitasking
            ComplexRule.Action.DisallowMultitasking ->
                ChangeComplexRuleActionViewData.Type.DisallowMultitasking
            ComplexRule.Action.SetTag ->
                ChangeComplexRuleActionViewData.Type.SetTag
        }
    }

    fun mapAction(
        action: ChangeComplexRuleActionViewData.Type,
    ): ComplexRule.Action {
        return when (action) {
            ChangeComplexRuleActionViewData.Type.AllowMultitasking ->
                ComplexRule.Action.AllowMultitasking
            ChangeComplexRuleActionViewData.Type.DisallowMultitasking ->
                ComplexRule.Action.DisallowMultitasking
            ChangeComplexRuleActionViewData.Type.SetTag ->
                ComplexRule.Action.SetTag
        }
    }

    fun mapActionTitle(
        action: ComplexRule.Action?,
        tagIds: Set<Long>,
    ): String {
        return when (action) {
            null -> {
                resourceRepo.getString(R.string.change_complex_rule_choose_action)
            }
            ComplexRule.Action.AllowMultitasking -> {
                resourceRepo.getString(R.string.settings_allow_multitasking)
            }
            ComplexRule.Action.DisallowMultitasking -> {
                resourceRepo.getString(R.string.settings_disallow_multitasking)
            }
            ComplexRule.Action.SetTag -> {
                val title = resourceRepo.getString(R.string.change_complex_action_set_tag)
                val selectedTags = tagIds.size
                    .takeIf { it != 0 }
                    ?.let { "($it)" }
                if (selectedTags != null) {
                    "$title $selectedTags"
                } else {
                    title
                }
            }
        }
    }
}
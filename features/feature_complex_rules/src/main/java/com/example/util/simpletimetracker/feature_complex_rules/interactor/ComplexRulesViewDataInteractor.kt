package com.example.util.simpletimetracker.feature_complex_rules.interactor

import com.example.util.simpletimetracker.core.mapper.ComplexRuleViewDataMapper
import com.example.util.simpletimetracker.domain.complexRule.interactor.ComplexRuleInteractor
import com.example.util.simpletimetracker.domain.complexRule.model.ComplexRule
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_complex_rules.mapper.ComplexRulesViewDataMapper
import javax.inject.Inject

class ComplexRulesViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val complexRuleInteractor: ComplexRuleInteractor,
    private val complexRuleViewDataMapper: ComplexRuleViewDataMapper,
    private val complexRulesViewDataMapper: ComplexRulesViewDataMapper,
) {

    suspend fun getViewData(): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val rules = complexRuleInteractor.getAll()
        val result: MutableList<ViewHolderType> = mutableListOf()
        val types = recordTypeInteractor.getAll()
        val typesMap = types.associateBy { it.id }
        val typeIds = types.map { it.id }
        val tags = recordTagInteractor.getAll()
        val tagsMap = tags.associateBy { it.id }
        val tagIds = tags.map { it.id }

        val sortByActionsList = listOf(
            ComplexRule.Action.AllowMultitasking,
            ComplexRule.Action.DisallowMultitasking,
            ComplexRule.Action.AssignTag,
        )
        val ruleGroups = rules.groupBy { it.action }
        val shouldAddHints = ruleGroups.size > 1

        result += complexRulesViewDataMapper.mapAddItem(isDarkTheme)

        sortByActionsList.forEach {
            val group = ruleGroups[it] ?: return@forEach
            if (shouldAddHints) {
                val anyAction = group.firstOrNull()?.action
                if (anyAction != null) {
                    val hint = complexRuleViewDataMapper.mapActionTitle(
                        action = anyAction,
                        disallowOnlyPrevious = false,
                    )
                    result += HintViewData(hint)
                }
            }
            group.forEach { rule ->
                result += complexRuleViewDataMapper.mapRule(
                    rule = rule,
                    isDarkTheme = isDarkTheme,
                    typesMap = typesMap,
                    tagsMap = tagsMap,
                    typesOrder = typeIds,
                    tagsOrder = tagIds,
                )
            }
        }

        return result
    }
}
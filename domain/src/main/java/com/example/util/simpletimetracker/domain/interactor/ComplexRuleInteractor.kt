package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.ComplexRule
import com.example.util.simpletimetracker.domain.repo.ComplexRuleRepo
import javax.inject.Inject

class ComplexRuleInteractor @Inject constructor(
    private val repo: ComplexRuleRepo,
) {

    suspend fun isEmpty(): Boolean {
        return repo.isEmpty()
    }

    suspend fun getAll(): List<ComplexRule> {
        return repo.getAll()
    }

    suspend fun get(id: Long): ComplexRule? {
        return repo.get(id)
    }

    suspend fun add(rule: ComplexRule): Long {
        return repo.add(rule)
    }

    suspend fun disable(id: Long) {
        repo.disable(id)
    }

    suspend fun enable(id: Long) {
        repo.enable(id)
    }

    suspend fun remove(id: Long) {
        repo.remove(id)
    }

    suspend fun removeTypeId(id: Long) {
        getAll().filter {
            id in it.conditionStartingTypeIds ||
                id in it.conditionCurrentTypeIds
        }.forEach { rule ->
            val newStartingTypeIds = rule.conditionStartingTypeIds
                .toMutableSet()
                .apply { remove(id) }
            val newCurrentTypeIds = rule.conditionCurrentTypeIds
                .toMutableSet()
                .apply { remove(id) }
            val newRule = rule.copy(
                conditionStartingTypeIds = newStartingTypeIds,
                conditionCurrentTypeIds = newCurrentTypeIds,
            )
            if (newRule.hasConditions) {
                add(newRule)
            } else {
                remove(newRule.id)
            }
        }
    }

    suspend fun removeTagId(id: Long) {
        getAll().filter {
            id in it.actionAssignTagIds
        }.forEach { rule ->
            val newActionAssignTagIds = rule.actionAssignTagIds
                .toMutableSet()
                .apply { remove(id) }
            val newRule = rule.copy(
                actionAssignTagIds = newActionAssignTagIds,
            )
            if (newRule.action is ComplexRule.Action.AssignTag &&
                newActionAssignTagIds.isEmpty()
            ) {
                remove(newRule.id)
            } else {
                add(newRule)
            }
        }
    }
}
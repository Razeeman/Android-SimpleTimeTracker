package com.example.util.simpletimetracker.domain.recordType.interactor

import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import javax.inject.Inject

class FilterActiveGoalsInteractor @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
) {

    suspend fun execute(goals: List<RecordTypeGoal>): List<RecordTypeGoal> {
        val activeTypeIds = if (goals.any { it.idData is RecordTypeGoal.IdData.Type }) {
            recordTypeInteractor.getAll().filterNot { it.hidden }.map { it.id }.toSet()
        } else {
            emptySet()
        }
        val activeTagIds = if (goals.any { it.idData is RecordTypeGoal.IdData.Tag }) {
            recordTagInteractor.getAll().filterNot { it.archived }.map { it.id }.toSet()
        } else {
            emptySet()
        }

        return goals.filter { goal ->
            when (val owner = goal.idData) {
                is RecordTypeGoal.IdData.Type -> owner.value in activeTypeIds
                is RecordTypeGoal.IdData.Tag -> owner.value in activeTagIds
                is RecordTypeGoal.IdData.Category -> true
            }
        }
    }

    suspend fun isActive(owner: RecordTypeGoal.IdData): Boolean {
        return when (owner) {
            is RecordTypeGoal.IdData.Type -> recordTypeInteractor.get(owner.value)?.hidden == false
            is RecordTypeGoal.IdData.Tag -> recordTagInteractor.get(owner.value)?.archived == false
            is RecordTypeGoal.IdData.Category -> true
        }
    }
}

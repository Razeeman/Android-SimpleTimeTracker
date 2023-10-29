package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.repo.RecordTypeGoalRepo
import javax.inject.Inject

class RecordTypeGoalInteractor @Inject constructor(
    private val repo: RecordTypeGoalRepo,
) {

    suspend fun getAll(): List<RecordTypeGoal> {
        return repo.getAll()
    }

    suspend fun getAllTypeGoals(): List<RecordTypeGoal> {
        return repo.getAllTypeGoals()
    }

    suspend fun getAllCategoryGoals(): List<RecordTypeGoal> {
        return repo.getAllCategoryGoals()
    }

    suspend fun getByType(typeId: Long): List<RecordTypeGoal> {
        return repo.getByType(typeId)
    }

    suspend fun getByCategory(categoryId: Long): List<RecordTypeGoal> {
        return repo.getByCategory(categoryId)
    }

    suspend fun add(recordTypeGoal: RecordTypeGoal) {
        repo.add(recordTypeGoal)
    }

    suspend fun remove(id: Long) {
        repo.remove(id)
    }
}
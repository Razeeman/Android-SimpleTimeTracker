package com.example.util.simpletimetracker.domain.recordType.interactor

import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.recordType.repo.RecordTypeGoalRepo
import javax.inject.Inject

class RecordTypeGoalInteractor @Inject constructor(
    private val repo: RecordTypeGoalRepo,
) {

    suspend fun getAll(): List<RecordTypeGoal> {
        return repo.getAll()
    }

    suspend fun get(id: Long): RecordTypeGoal? {
        return repo.get(id)
    }

    suspend fun getAllTypeGoals(): List<RecordTypeGoal> {
        return repo.getAllTypeGoals()
    }

    suspend fun getAllCategoryGoals(): List<RecordTypeGoal> {
        return repo.getAllCategoryGoals()
    }

    suspend fun getAllTagGoals(): List<RecordTypeGoal> {
        return repo.getAllTagGoals()
    }

    suspend fun getByType(typeId: Long): List<RecordTypeGoal> {
        return repo.getByType(typeId)
    }

    suspend fun getByCategory(categoryId: Long): List<RecordTypeGoal> {
        return repo.getByCategory(categoryId)
    }

    suspend fun getByTag(tagId: Long): List<RecordTypeGoal> {
        return repo.getByTag(tagId)
    }

    suspend fun add(recordTypeGoal: RecordTypeGoal) {
        repo.add(recordTypeGoal)
    }

    suspend fun remove(id: Long) {
        repo.remove(id)
    }
}
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
        return repo.getAll().filterByIdData<RecordTypeGoal.IdData.Type>()
    }

    suspend fun getAllCategoryGoals(): List<RecordTypeGoal> {
        return repo.getAll().filterByIdData<RecordTypeGoal.IdData.Category>()
    }

    suspend fun getAllTagGoals(): List<RecordTypeGoal> {
        return repo.getAll().filterByIdData<RecordTypeGoal.IdData.Tag>()
    }

    suspend fun getByType(typeId: Long): List<RecordTypeGoal> {
        return repo.getByOwnerId(typeId).filterByIdData<RecordTypeGoal.IdData.Type>()
    }

    suspend fun getByCategory(categoryId: Long): List<RecordTypeGoal> {
        return repo.getByOwnerId(categoryId).filterByIdData<RecordTypeGoal.IdData.Category>()
    }

    suspend fun getByTag(tagId: Long): List<RecordTypeGoal> {
        return repo.getByOwnerId(tagId).filterByIdData<RecordTypeGoal.IdData.Tag>()
    }

    suspend fun add(recordTypeGoal: RecordTypeGoal) {
        repo.add(recordTypeGoal)
    }

    suspend fun remove(id: Long) {
        repo.remove(id)
    }

    suspend fun removeByType(typeId: Long) {
        repo.removeByType(typeId)
    }

    suspend fun removeByCategory(categoryId: Long) {
        repo.removeByCategory(categoryId)
    }

    suspend fun removeByTag(tagId: Long) {
        repo.removeByTag(tagId)
    }

    private inline fun <reified T : RecordTypeGoal.IdData> List<RecordTypeGoal>.filterByIdData(): List<RecordTypeGoal> {
        return this.filter { it.idData is T }
    }
}
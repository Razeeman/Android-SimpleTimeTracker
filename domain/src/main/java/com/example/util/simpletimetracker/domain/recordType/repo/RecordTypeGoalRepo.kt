package com.example.util.simpletimetracker.domain.recordType.repo

import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal

interface RecordTypeGoalRepo {

    suspend fun getAll(): List<RecordTypeGoal>

    suspend fun get(id: Long): RecordTypeGoal?

    suspend fun getAllTypeGoals(): List<RecordTypeGoal>

    suspend fun getAllCategoryGoals(): List<RecordTypeGoal>

    suspend fun getAllTagGoals(): List<RecordTypeGoal>

    suspend fun getByType(typeId: Long): List<RecordTypeGoal>

    suspend fun getByCategory(categoryId: Long): List<RecordTypeGoal>

    suspend fun getByTag(tagId: Long): List<RecordTypeGoal>

    suspend fun add(recordTypeGoal: RecordTypeGoal): Long

    suspend fun remove(id: Long)

    suspend fun removeByType(typeId: Long)

    suspend fun removeByCategory(categoryId: Long)

    suspend fun removeByTag(tagId: Long)

    suspend fun clear()
}
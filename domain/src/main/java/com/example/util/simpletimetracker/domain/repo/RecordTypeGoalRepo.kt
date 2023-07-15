package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.RecordTypeGoal

interface RecordTypeGoalRepo {

    suspend fun getAll(): List<RecordTypeGoal>

    suspend fun getByType(typeId: Long): List<RecordTypeGoal>

    suspend fun add(recordTypeGoal: RecordTypeGoal): Long

    suspend fun remove(id: Long)

    suspend fun removeByType(typeId: Long)

    suspend fun clear()
}
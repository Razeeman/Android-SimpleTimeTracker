package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.RunningRecord

interface RunningRecordRepo {

    suspend fun getAll(): List<RunningRecord>

    suspend fun get(id: Long): RunningRecord?

    suspend fun add(runningRecord: RunningRecord)

    suspend fun remove(id: Long)

    suspend fun removeTag(tagId: Long)

    suspend fun clear()
}
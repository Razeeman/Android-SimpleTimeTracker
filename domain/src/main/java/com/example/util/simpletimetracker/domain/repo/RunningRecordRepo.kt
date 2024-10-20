package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.RunningRecord

interface RunningRecordRepo {

    suspend fun isEmpty(): Boolean

    suspend fun getAll(): List<RunningRecord>

    suspend fun get(id: Long): RunningRecord?

    suspend fun has(id: Long): Boolean

    suspend fun add(runningRecord: RunningRecord): Long

    suspend fun remove(id: Long)

    suspend fun clear()
}
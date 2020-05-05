package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.RunningRecord

interface BaseRunningRecordRepo {

    suspend fun getAll(): List<RunningRecord>

    suspend fun add(runningRecord: RunningRecord)

    suspend fun remove(name: String)

    suspend fun clear()
}
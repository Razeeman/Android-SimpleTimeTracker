package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.domain.repo.RunningRecordRepo
import javax.inject.Inject

class RunningRecordInteractor @Inject constructor(
    private val runningRecordRepo: RunningRecordRepo
) {

    suspend fun getAll(): List<RunningRecord> {
        return runningRecordRepo.getAll()
    }

    suspend fun get(id: Long): RunningRecord? {
        return runningRecordRepo.get(id)
    }

    suspend fun add(typeId: Long, timeStarted: Long? = null) {
        if (get(typeId) == null) {
            RunningRecord(
                id = typeId,
                timeStarted = timeStarted ?: System.currentTimeMillis()
            ).let { runningRecordRepo.add(it) }
        }
    }

    suspend fun remove(id: Long) {
        runningRecordRepo.remove(id)
    }

    suspend fun clear() {
        runningRecordRepo.clear()
    }
}
package com.example.util.simpletimetracker.domain.record.interactor

import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.record.repo.RunningRecordRepo
import javax.inject.Inject

class RunningRecordInteractor @Inject constructor(
    private val runningRecordRepo: RunningRecordRepo,
) {

    suspend fun isEmpty(): Boolean {
        return runningRecordRepo.isEmpty()
    }

    suspend fun getAll(): List<RunningRecord> {
        return runningRecordRepo.getAll()
    }

    suspend fun get(id: Long): RunningRecord? {
        return runningRecordRepo.get(id)
    }

    suspend fun has(id: Long): Boolean {
        return runningRecordRepo.has(id)
    }

    suspend fun add(runningRecord: RunningRecord) {
        runningRecordRepo.add(runningRecord)
    }

    suspend fun remove(id: Long) {
        runningRecordRepo.remove(id)
    }
}
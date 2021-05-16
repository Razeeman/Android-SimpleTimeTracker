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

    suspend fun add(runningRecord: RunningRecord) {
        runningRecordRepo.add(runningRecord)
    }

    suspend fun remove(id: Long) {
        runningRecordRepo.remove(id)
    }

    suspend fun removeTag(tagId: Long) {
        runningRecordRepo.removeTag(tagId)
    }

    suspend fun clear() {
        runningRecordRepo.clear()
    }
}
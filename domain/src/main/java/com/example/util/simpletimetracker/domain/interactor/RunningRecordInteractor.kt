package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.domain.repo.RunningRecordRepo
import com.example.util.simpletimetracker.domain.repo.RunningRecordToRecordTagRepo
import javax.inject.Inject

class RunningRecordInteractor @Inject constructor(
    private val runningRecordRepo: RunningRecordRepo,
    private val runningRecordToRecordTagRepo: RunningRecordToRecordTagRepo,
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

    suspend fun add(runningRecord: RunningRecord) {
        val recordId = runningRecordRepo.add(runningRecord)
        runningRecordToRecordTagRepo.removeAllByRunningRecordId(recordId)
        runningRecordToRecordTagRepo.addRunningRecordTags(recordId, runningRecord.tagIds)
    }

    suspend fun remove(id: Long) {
        runningRecordToRecordTagRepo.removeAllByRunningRecordId(id)
        runningRecordRepo.remove(id)
    }
}
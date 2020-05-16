package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.domain.repo.RunningRecordRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RunningRecordInteractor @Inject constructor(
    private val runningRecordRepo: RunningRecordRepo
) {

    suspend fun getAll(): List<RunningRecord> = withContext(Dispatchers.IO) {
        runningRecordRepo.getAll()
    }

    suspend fun get(id: Long): RunningRecord? = withContext(Dispatchers.IO) {
        runningRecordRepo.get(id)
    }

    suspend fun add(runningRecord: RunningRecord) = withContext(Dispatchers.IO) {
        runningRecordRepo.add(runningRecord)
    }

    suspend fun remove(id: Long) = withContext(Dispatchers.IO) {
        runningRecordRepo.remove(id)
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        runningRecordRepo.clear()
    }
}
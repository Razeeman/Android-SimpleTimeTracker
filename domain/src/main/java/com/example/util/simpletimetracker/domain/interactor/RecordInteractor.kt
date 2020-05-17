package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecordInteractor @Inject constructor(
    private val recordRepo: RecordRepo
) {

    suspend fun getAll(): List<Record> = withContext(Dispatchers.IO) {
        recordRepo.getAll()
    }

    suspend fun get(id: Long): Record? = withContext(Dispatchers.IO) {
        recordRepo.get(id)
    }

    suspend fun getFromRange(start: Long, end: Long): List<Record> = withContext(Dispatchers.IO) {
        recordRepo.getFromRange(start, end)
    }

    suspend fun add(typeId: Long, timeStarted: Long) = withContext(Dispatchers.IO) {
        Record(
            typeId = typeId,
            timeStarted = timeStarted,
            timeEnded = System.currentTimeMillis()
        ).let {
            recordRepo.add(it)
        }
    }

    suspend fun add(record: Record) = withContext(Dispatchers.IO) {
        recordRepo.add(record)
    }

    suspend fun remove(id: Long) = withContext(Dispatchers.IO) {
        recordRepo.remove(id)
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        recordRepo.clear()
    }
}
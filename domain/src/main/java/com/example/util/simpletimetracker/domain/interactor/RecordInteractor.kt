package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.repo.BaseRecordRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecordInteractor @Inject constructor(
    private val recordRepo: BaseRecordRepo
) {

    suspend fun getAll(): List<Record> = withContext(Dispatchers.IO) {
        recordRepo.getAll()
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
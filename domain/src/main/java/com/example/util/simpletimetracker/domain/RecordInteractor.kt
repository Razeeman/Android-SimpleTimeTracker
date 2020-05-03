package com.example.util.simpletimetracker.domain

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

    suspend fun clear() = withContext(Dispatchers.IO) {
        recordRepo.clear()
    }
}
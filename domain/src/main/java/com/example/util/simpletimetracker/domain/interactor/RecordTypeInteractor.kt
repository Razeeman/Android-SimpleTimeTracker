package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.repo.BaseRecordTypeRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecordTypeInteractor @Inject constructor(
    private val recordTypeRepo: BaseRecordTypeRepo
) {

    suspend fun getAll(): List<RecordType> = withContext(Dispatchers.IO) {
        recordTypeRepo.getAll()
    }

    suspend fun add(recordType: RecordType) = withContext(Dispatchers.IO) {
        recordTypeRepo.add(recordType)
    }

    suspend fun remove(name: String) = withContext(Dispatchers.IO) {
        recordTypeRepo.remove(name)
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        recordTypeRepo.clear()
    }
}
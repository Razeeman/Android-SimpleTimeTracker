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

    suspend fun get(id: Long): RecordType? = withContext(Dispatchers.IO) {
        recordTypeRepo.get(id)
    }

    suspend fun add(recordType: RecordType) = withContext(Dispatchers.IO) {
        var newRecord = recordType

        // If there is already an item with this name - override
        recordTypeRepo.getAll()
            .firstOrNull { saved ->
                saved.name == recordType.name
            }
            ?.let { saved ->
                newRecord = recordType.copy(id = saved.id)
            }

        recordTypeRepo.add(newRecord)
    }

    suspend fun remove(id: Long) = withContext(Dispatchers.IO) {
        recordTypeRepo.remove(id)
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        recordTypeRepo.clear()
    }
}
package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.repo.RecordTypeRepo
import javax.inject.Inject

class RecordTypeInteractor @Inject constructor(
    private val recordTypeRepo: RecordTypeRepo
) {

    suspend fun getAll(): List<RecordType> {
        return recordTypeRepo.getAll()
    }

    suspend fun get(id: Long): RecordType? {
        return recordTypeRepo.get(id)
    }

    suspend fun add(recordType: RecordType) {
        var newRecord = recordType

        // If there is already an item with this name - override
        recordTypeRepo.getAll() // TODO get by name
            .firstOrNull { saved ->
                saved.name == recordType.name
            }
            ?.let { saved ->
                newRecord = recordType.copy(id = saved.id)
            }

        recordTypeRepo.add(newRecord)
    }

    suspend fun remove(id: Long) {
        recordTypeRepo.remove(id)
    }

    suspend fun clear() {
        recordTypeRepo.clear()
    }
}
package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.repo.RecordTypeCacheRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeRepo
import java.util.Locale
import javax.inject.Inject

class RecordTypeInteractor @Inject constructor(
    private val recordTypeRepo: RecordTypeRepo,
    private val recordTypeCacheRepo: RecordTypeCacheRepo,
    private val prefsInteractor: PrefsInteractor
) {

    suspend fun getAll(): List<RecordType> {
        return (recordTypeCacheRepo.getAll()
            .takeIf(List<RecordType>::isNotEmpty)
            ?: recordTypeRepo.getAll().also(recordTypeCacheRepo::addAll))
            .sortedBy { it.name.toLowerCase(Locale.getDefault()) }
            .let {
                when (prefsInteractor.getRecordTypesOrder()) {
                    CardOrder.COLOR -> {
                        it.sortedBy(RecordType::color)
                    }
                    CardOrder.MANUAL -> {
                        val order = prefsInteractor.getCardsOrderManual()
                        it
                            .map { type -> type to order.getOrElse(type.id, { 0 }) }
                            .sortedBy { (_, order) -> order }
                            .map { (type, _) -> type }
                    }
                    CardOrder.NAME -> {
                        it
                    }
                }
            }
    }

    suspend fun get(id: Long): RecordType? {
        return recordTypeRepo.get(id)
    }

    suspend fun add(recordType: RecordType) {
        var newRecord = recordType

        // If there is already an item with this name - override
        recordTypeRepo.get(recordType.name)
            ?.let { saved ->
                newRecord = recordType.copy(id = saved.id)
            }

        recordTypeRepo.add(newRecord)
        recordTypeCacheRepo.clear()
    }

    suspend fun remove(id: Long) {
        recordTypeRepo.remove(id)
        recordTypeCacheRepo.clear()
    }

    suspend fun clear() {
        recordTypeRepo.clear()
        recordTypeCacheRepo.clear()
    }
}
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
            .let{ sort(it) }
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

    private suspend fun sort(records: List<RecordType>): List<RecordType> {
        return records
            .let(::sortByName)
            .let {
                when (prefsInteractor.getCardOrder()) {
                    CardOrder.COLOR -> sortByColor(it)
                    CardOrder.MANUAL -> sortByManualOrder(it)
                    CardOrder.NAME -> it
                }
            }
    }

    private fun sortByName(records: List<RecordType>): List<RecordType> {
        return records.sortedBy { it.name.toLowerCase(Locale.getDefault()) }
    }

    private fun sortByColor(records: List<RecordType>): List<RecordType> {
        return records.sortedBy(RecordType::color)
    }

    private suspend fun sortByManualOrder(records: List<RecordType>): List<RecordType> {
        val order = prefsInteractor.getCardOrderManual()
        return records
            .map { type -> type to order.getOrElse(type.id, { 0 }) }
            .sortedBy { (_, order) -> order }
            .map { (type, _) -> type }
    }
}
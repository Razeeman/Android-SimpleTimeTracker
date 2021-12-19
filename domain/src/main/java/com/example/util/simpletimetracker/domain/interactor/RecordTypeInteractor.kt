package com.example.util.simpletimetracker.domain.interactor

import android.graphics.Color
import com.example.util.simpletimetracker.domain.mapper.AppColorMapper
import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import com.example.util.simpletimetracker.domain.repo.RecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeCacheRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeCategoryRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeRepo
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

class RecordTypeInteractor @Inject constructor(
    private val recordTypeRepo: RecordTypeRepo,
    private val recordRepo: RecordRepo,
    private val recordToRecordTagRepo: RecordToRecordTagRepo,
    private val recordTypeCategoryRepo: RecordTypeCategoryRepo,
    private val recordTagRepo: RecordTagRepo,
    private val recordTypeCacheRepo: RecordTypeCacheRepo,
    private val prefsInteractor: PrefsInteractor,
    private val appColorMapper: AppColorMapper,
) {

    suspend fun getAll(cardOrder: CardOrder? = null): List<RecordType> {
        return (
            recordTypeCacheRepo.getAll()
                .takeIf(List<RecordType>::isNotEmpty)
                ?: recordTypeRepo.getAll().also(recordTypeCacheRepo::addAll)
            ).let { sort(cardOrder, it) }
    }

    suspend fun get(id: Long): RecordType? {
        return recordTypeRepo.get(id)
    }

    suspend fun add(recordType: RecordType): Long {
        var newRecord = recordType

        // If there is already an item with this name - override
        recordTypeRepo.get(recordType.name)
            ?.let { saved ->
                newRecord = recordType.copy(id = saved.id)
            }

        val addedId = recordTypeRepo.add(newRecord)
        recordTypeCacheRepo.clear()
        return addedId
    }

    suspend fun archive(id: Long) {
        recordTypeRepo.archive(id)
        recordTypeCacheRepo.clear()
    }

    suspend fun restore(id: Long) {
        recordTypeRepo.restore(id)
        recordTypeCacheRepo.clear()
    }

    suspend fun remove(id: Long) {
        val recordsToRemove = recordRepo.getByType(listOf(id)).map { it.id }
        recordsToRemove.forEach { recordId ->
            recordToRecordTagRepo.removeAllByRecordId(recordId) // TODO do better?
        }
        val tagsToRemove = recordTagRepo.getByType(id).map { it.id }
        tagsToRemove.forEach { recordId ->
            recordToRecordTagRepo.removeAllByRecordId(recordId) // TODO do better?
        }

        recordRepo.removeByType(id)
        recordTypeCategoryRepo.removeAllByType(id)
        recordTagRepo.removeByType(id)
        recordTypeRepo.remove(id)

        recordTypeCacheRepo.clear()
    }

    suspend fun clear() {
        recordTypeRepo.clear()
        recordTypeCacheRepo.clear()
    }

    private suspend fun sort(
        cardOrder: CardOrder?,
        records: List<RecordType>,
    ): List<RecordType> {
        return records
            .let(::sortByName)
            .let {
                when (cardOrder ?: prefsInteractor.getCardOrder()) {
                    CardOrder.COLOR -> sortByColor(it)
                    CardOrder.MANUAL -> sortByManualOrder(it)
                    CardOrder.NAME -> it
                }
            }
    }

    private fun sortByName(records: List<RecordType>): List<RecordType> {
        return records.sortedBy { it.name.lowercase(Locale.getDefault()) }
    }

    private fun sortByColor(types: List<RecordType>): List<RecordType> {
        return types
            .map { type ->
                type to appColorMapper.mapToColorInt(color = type.color)
            }
            .map { (type, colorInt) ->
                val hsv = FloatArray(3)
                Color.colorToHSV(colorInt, hsv)
                type to hsv
            }
            .sortedWith(
                compareBy(
                    // Round to int to prevent wiggling around floating points.
                    { -(it.second[0].roundToInt()) }, // reversed hue
                    { (it.second[1] * 100).roundToInt() }, // saturation
                    { (it.second[2] * 100).roundToInt() }, // value
                )
            )
            .map { (type, _) ->
                type
            }
    }

    private suspend fun sortByManualOrder(records: List<RecordType>): List<RecordType> {
        val order = prefsInteractor.getCardOrderManual()
        return records
            .map { type -> type to order.getOrElse(type.id, { 0 }) }
            .sortedBy { (_, order) -> order }
            .map { (type, _) -> type }
    }
}
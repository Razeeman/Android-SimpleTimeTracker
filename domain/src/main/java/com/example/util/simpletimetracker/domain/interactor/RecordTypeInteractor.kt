package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import com.example.util.simpletimetracker.domain.repo.RecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeCategoryRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeGoalRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeToDefaultTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeToTagRepo
import com.example.util.simpletimetracker.domain.repo.RunningRecordRepo
import javax.inject.Inject

class RecordTypeInteractor @Inject constructor(
    private val recordTypeRepo: RecordTypeRepo,
    private val recordRepo: RecordRepo,
    private val runningRecordRepo: RunningRecordRepo,
    private val recordTagRepo: RecordTagRepo,
    private val recordToRecordTagRepo: RecordToRecordTagRepo,
    private val recordTypeCategoryRepo: RecordTypeCategoryRepo,
    private val recordTypeToTagRepo: RecordTypeToTagRepo,
    private val recordTypeToDefaultTagRepo: RecordTypeToDefaultTagRepo,
    private val recordTypeGoalRepo: RecordTypeGoalRepo,
    private val complexRuleInteractor: ComplexRuleInteractor,
    private val activityFilterInteractor: ActivityFilterInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val sortCardsInteractor: SortCardsInteractor,
) {

    suspend fun getAll(cardOrder: CardOrder? = null): List<RecordType> {
        return sortCardsInteractor.sort(
            cardOrder = cardOrder ?: prefsInteractor.getCardOrder(),
            manualOrderProvider = { prefsInteractor.getCardOrderManual() },
            data = recordTypeRepo.getAll().map(::mapForSort),
        ).map { it.data }
    }

    suspend fun get(id: Long): RecordType? {
        return recordTypeRepo.get(id)
    }

    suspend fun get(name: String): RecordType? {
        return recordTypeRepo.get(name)
    }

    suspend fun add(recordType: RecordType): Long {
        return recordTypeRepo.add(recordType)
    }

    suspend fun archive(id: Long) {
        recordTypeRepo.archive(id)
    }

    suspend fun restore(id: Long) {
        recordTypeRepo.restore(id)
    }

    suspend fun remove(id: Long) {
        val recordsToRemove = recordRepo.getByType(listOf(id)).map { it.id }
        recordsToRemove.forEach { recordId ->
            recordToRecordTagRepo.removeAllByRecordId(recordId) // TODO do better?
        }
        val tagsToChange = recordTagRepo.getByType(id)
        if (tagsToChange.isNotEmpty()) {
            val type = recordTypeRepo.get(id)
            if (type != null) {
                tagsToChange.forEach { tag ->
                    val updatedTag = tag.copy(
                        color = type.color,
                        icon = type.icon,
                        iconColorSource = 0,
                    )
                    recordTagRepo.add(updatedTag)
                }
            }
        }
        prefsInteractor.getRecordTagSelectionExcludeActivities().toMutableList()
            .apply { remove(id) }
            .let { prefsInteractor.setRecordTagSelectionExcludeActivities(it) }
        recordRepo.removeByType(id)
        runningRecordRepo.remove(id)
        recordTypeCategoryRepo.removeAllByType(id)
        recordTypeToTagRepo.removeAllByType(id)
        recordTypeToDefaultTagRepo.removeAllByType(id)
        recordTypeGoalRepo.removeByType(id)
        complexRuleInteractor.removeTypeId(id)
        activityFilterInteractor.removeTypeId(id)
        recordTypeRepo.remove(id)
    }

    fun mapForSort(
        data: RecordType,
    ): SortCardsInteractor.DataHolder<RecordType> {
        return SortCardsInteractor.DataHolder(
            id = data.id,
            name = data.name,
            color = data.color,
            data = data,
        )
    }
}
package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.repo.RecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeToTagRepo
import com.example.util.simpletimetracker.domain.repo.RunningRecordToRecordTagRepo
import javax.inject.Inject

class RecordTagInteractor @Inject constructor(
    private val repo: RecordTagRepo,
    private val recordToRecordTagRepo: RecordToRecordTagRepo,
    private val runningRecordToRecordTagRepo: RunningRecordToRecordTagRepo,
    private val recordTypeToTagRepo: RecordTypeToTagRepo,
    private val prefsInteractor: PrefsInteractor,
    private val sortCardsInteractor: SortCardsInteractor,
) {

    suspend fun isEmpty(): Boolean {
        return repo.isEmpty()
    }

    suspend fun getAll(cardOrder: CardOrder? = null): List<RecordTag> {
        return sortCardsInteractor.sort(
            cardOrder = cardOrder ?: prefsInteractor.getTagOrder(),
            manualOrderProvider = { prefsInteractor.getTagOrderManual() },
            data = repo.getAll().map(::mapForSort),
        ).map { it.data }
    }

    suspend fun get(id: Long): RecordTag? {
        return repo.get(id)
    }

    // TODO check if already has this name, show alert.
    // TODO same for activities.
    suspend fun add(tag: RecordTag): Long {
        return repo.add(tag)
    }

    suspend fun archive(id: Long) {
        repo.archive(id)
    }

    suspend fun restore(id: Long) {
        repo.restore(id)
    }

    suspend fun remove(id: Long) {
        repo.remove(id)
        recordToRecordTagRepo.removeAllByTagId(id)
        runningRecordToRecordTagRepo.removeAllByTagId(id)
        recordTypeToTagRepo.removeAll(id)
    }

    private fun mapForSort(
        data: RecordTag,
    ): SortCardsInteractor.DataHolder<RecordTag> {
        return SortCardsInteractor.DataHolder(
            id = data.id,
            name = data.name,
            color = data.color,
            data = data,
        )
    }
}
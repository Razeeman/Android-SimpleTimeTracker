package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.repo.RecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeToTagRepo
import com.example.util.simpletimetracker.domain.repo.RunningRecordToRecordTagRepo
import java.util.Locale
import javax.inject.Inject

class RecordTagInteractor @Inject constructor(
    private val repo: RecordTagRepo,
    private val recordToRecordTagRepo: RecordToRecordTagRepo,
    private val runningRecordToRecordTagRepo: RunningRecordToRecordTagRepo,
    private val recordTypeToTagRepo: RecordTypeToTagRepo,
) {

    suspend fun isEmpty(): Boolean {
        return repo.isEmpty()
    }

    suspend fun getAll(): List<RecordTag> {
        // TODO TAGS add sort
        return repo.getAll().let(::sort)
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

    // TODO remove sort and sort when needed.
    private fun sort(items: List<RecordTag>): List<RecordTag> {
        return items.sortedBy { it.name.lowercase(Locale.getDefault()) }
    }
}
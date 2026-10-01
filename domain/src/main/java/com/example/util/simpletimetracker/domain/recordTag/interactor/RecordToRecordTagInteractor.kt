package com.example.util.simpletimetracker.domain.recordTag.interactor

import com.example.util.simpletimetracker.domain.recordTag.repo.RecordToRecordTagRepo
import javax.inject.Inject

class RecordToRecordTagInteractor @Inject constructor(
    private val repo: RecordToRecordTagRepo,
) {

    suspend fun getRecordIdsByTagId(tagId: Long): List<Long> {
        return repo.getRecordIdsByTagId(tagId)
    }

    suspend fun getRecordCountsByTag(): Map<Long, Int> {
        return repo.getRecordCountsByTag()
    }
}
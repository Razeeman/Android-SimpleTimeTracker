package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.repo.RecordToRecordTagRepo
import javax.inject.Inject

class RecordToRecordTagInteractor @Inject constructor(
    private val repo: RecordToRecordTagRepo,
) {

    suspend fun getRecordIdsByTagId(tagId: Long): List<Long> {
        return repo.getRecordIdsByTagId(tagId)
    }
}
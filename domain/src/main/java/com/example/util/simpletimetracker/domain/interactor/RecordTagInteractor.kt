package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.repo.RecordTagRepo
import javax.inject.Inject

class RecordTagInteractor @Inject constructor(
    private val repo: RecordTagRepo
) {

    suspend fun getAll(): List<RecordTag> {
        return repo.getAll()
    }
}
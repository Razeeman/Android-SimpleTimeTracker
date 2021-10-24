package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.repo.RunningRecordToRecordTagRepo
import javax.inject.Inject

class RunningRecordToRecordTagInteractor @Inject constructor(
    private val repo: RunningRecordToRecordTagRepo,
) {

    suspend fun clear() {
        repo.clear()
    }
}
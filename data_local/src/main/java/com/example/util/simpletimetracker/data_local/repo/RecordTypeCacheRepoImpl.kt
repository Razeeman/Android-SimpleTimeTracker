package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.repo.RecordTypeCacheRepo
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordTypeCacheRepoImpl @Inject constructor() : RecordTypeCacheRepo {

    private var recordTypes: List<RecordType> = emptyList()

    override fun getAll(): List<RecordType> {
        Timber.d("getAll")
        return recordTypes
    }

    override fun addAll(recordTypes: List<RecordType>) {
        Timber.d("addAll")
        this.recordTypes = recordTypes
    }

    override fun clear() {
        Timber.d("clear")
        recordTypes = emptyList()
    }
}